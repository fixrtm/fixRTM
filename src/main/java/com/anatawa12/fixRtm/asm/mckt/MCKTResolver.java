package com.anatawa12.fixRtm.asm.mckt;

// SPDX-License-Identifier: MIT
// Copyright (c) 2021 anatawa12

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * <p>
 * The utility to resolve kotlin versions between two or more mods.
 * Collect all kotlin in mods dir and find the latest version.
 * This also resolves kotlin-stdlib-jdk7, kotlin-stdlib-jdk8, and kotlin-reflect.
 * </p>
 *
 * <p>
 * Kotlin Paths
 * </p>
 *
 * <lo>
 * <li>jars in mods directory (finds 'kotlin.Pair' and 'kotlin.KotlinVersion' class)</li>
 * <li>jar file specified in MANIFEST.MF</li>
 * </lo>
 *
 * <p>
 * Kotlin Cache Paths
 * </p>
 *
 * <lo>
 * <li>(get only) gradle modules-2.1 cache</li>
 * <li>(get only) maven local repository</li>
 * <li>(get or put) MCKTResolver cache directory ({@code '.cache/anatawa12-mckt-resolver/kotlin-stdlib/&lt;name&gt;.jar'})</li>
 * </lo>
 */
@SuppressWarnings({
   "unused", "RedundantSuppression", "JavacQuirks", "NullableProblems",
   "TryWithIdenticalCatches", "TryFinallyCanBeTryWithResources",
   "Convert2Diamond", "Convert2Lambda", "UseCompareMethod",
   "ResultOfMethodCallIgnored",
})
public class MCKTResolver {
   private MCKTResolver() {
   }

   static {
      // load classes first
      ResolveCaller.staticInit();
      Resolver.staticInit();
      SingleJarClassLoader.staticInit();
      GradleKotlinCacheFinder.staticInit();
      MavenKotlinCacheFinder.staticInit();
      MCKTKotlinCacheFinder.staticInit();
      DigesterOutputStream.staticInit();
      KotlinLibrary.staticInit();
      KotlinVersion.staticInit();
   }

   @SuppressWarnings("unused")
   public static void requestResolve() {
      Launch.classLoader.addClassLoaderExclusion("kotlin.");
      Double version = getDouble(resolverVersionPropName);
      if (version == null) {
         @SuppressWarnings("unchecked")
         List<String> tweakers = (List<String>) Launch.blackboard.get("TweakClasses");
         tweakers.add(ResolveCaller.class.getName());
      }
      if (needUpdate(version)) {
         System.setProperty(resolverVersionPropName, String.valueOf(RESOLVER_VERSION));
         System.setProperty(resolverNamePropName, Resolver.class.getName());
      }
   }

   public static Double getDouble(String nm) {
      String v = null;
      try {
         v = System.getProperty(nm);
      } catch (IllegalArgumentException ignored) {
      } catch (NullPointerException ignored) {
      }
      if (v != null) {
         try {
            return Double.parseDouble(v);
         } catch (NumberFormatException ignored) {
         }
      }
      return null;
   }

   private static boolean needUpdate(Double version) {
      if (version == null) return true;
      return version < RESOLVER_VERSION;
   }

   public static class ResolveCaller implements ITweaker {
      boolean run = false;

      public ResolveCaller() {
         @SuppressWarnings("unchecked")
         List<ITweaker> tweakers = (List<ITweaker>) Launch.blackboard.get("Tweaks");
         // add first
         tweakers.add(0, this);
      }

      @Override
      public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
         if (run) return;
         run = true;
         String className = System.getProperty(resolverNamePropName);
         try {
            Class<?> resolver = Class.forName(className);
            Method runResolve = resolver.getDeclaredMethod("runResolve",
               List.class,
               File.class,
               File.class,
               String.class);
            runResolve.setAccessible(true);
            runResolve.invoke(null, args, gameDir, assetsDir, profile);
         } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
         } catch (InvocationTargetException e) {
            Throwable throwable = e.getTargetException();
            if (throwable instanceof RuntimeException)
               throw (RuntimeException) throwable;
            throw new IllegalStateException(e.getMessage(), e);
         } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e.getMessage(), e);
         } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
         }
      }

      @Override
      public void injectIntoClassLoader(LaunchClassLoader classLoader) {
      }

      @Override
      public String getLaunchTarget() {
         return null;
      }

      @Override
      public String[] getLaunchArguments() {
         return new String[0];
      }

      public static void staticInit() {
      }
   }

   static class Resolver {
      int[] version;
      EnumSet<KotlinLibrary> libs = EnumSet.noneOf(KotlinLibrary.class);
      EnumMap<KotlinLibrary, URL> bundledElements = new EnumMap<KotlinLibrary, URL>(KotlinLibrary.class);
      KotlinCacheFinder[] finders = new KotlinCacheFinder[]{
         new GradleKotlinCacheFinder(),
         new MavenKotlinCacheFinder(),
         new MCKTKotlinCacheFinder(),
      };

      static void runResolve(List<String> args, File gameDir, File assetsDir, String profile) throws MalformedURLException {
         boolean nogui = GraphicsEnvironment.isHeadless();
         for (String arg : args) {
            if ("--nogui".equals(arg) || "nogui".equals(arg)) {
               nogui = true;
               break;
            }
         }
         new Resolver().runResolve(nogui);
      }

      void runResolve(boolean nogui) throws MalformedURLException {
         collectKotlinMods();

         String version = this.version[0] + "." + this.version[1] + "." + this.version[2];

         log("Kotlin version " + version + " with " + libs + "found!");

         EnumSet<KotlinLibrary> libraries = candidateDownloads();

         log(libraries + " missing in jars! Try cache then download!");

         for (KotlinLibrary library : libraries) {
            for (KotlinCacheFinder finder : finders) {
               File file = finder.find(library, version);
               if (file != null) {
                  log("cache found for " + library + " with " + finder);
                  bundledElements.put(library, file.toURI().toURL());
                  break;
               }
            }

            if (!bundledElements.containsKey(library)) {
               // download
               File downloaded;
               try {
                  downloaded = download(library, version);
               } catch (IOException e) {
                  throw new RuntimeException(e.getMessage(), e);
               }
               log("downloaded " + library);
               bundledElements.put(library, downloaded.toURI().toURL());
            }
         }

         for (Map.Entry<KotlinLibrary, URL> entry : bundledElements.entrySet()) {
            log("copying jar in jar from " + entry.getKey());
            URL value = entry.getValue();
            if (value.getProtocol().equals("jar")) {
               try {
                  File file = File.createTempFile("mckt", ".jar");
                  InputStream in = null;
                  OutputStream out = null;
                  DigesterOutputStream digester = new DigesterOutputStream(getSha1());
                  try {
                     in = new BufferedInputStream(value.openStream());
                     out = new BufferedOutputStream(new FileOutputStream(file));
                     copy(in, out, digester);
                  } finally {
                     if (in != null) in.close();
                     if (out != null) out.close();
                  }
                  entry.setValue(file.toURI().toURL());
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }
         }

         try {
            Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrl.setAccessible(true);
            for (URL value : bundledElements.values()) {
               addUrl.invoke(parentLoader, value);
            }
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
         }
         classLoader.addClassLoaderExclusion("kotlin.");
      }

      File download(KotlinLibrary library, String version) throws IOException {
         File writeTo = MCKTKotlinCacheFinder.getCacheFile(library, version);
         String url = "https://repo1.maven.org/maven2/" + library.architectPath(version);
         writeTo.getParentFile().mkdirs();
         InputStream in = null;
         OutputStream out = null;
         DigesterOutputStream digester = new DigesterOutputStream(getSha1());
         try {
            in = new BufferedInputStream(new URL(url).openStream());
            out = new BufferedOutputStream(new FileOutputStream(writeTo));
            copy(in, out, digester);
         } finally {
            if (in != null) in.close();
            if (out != null) out.close();
            out = null;
         }

         try {
            out = new BufferedOutputStream(new FileOutputStream(writeTo + ".sha1"));
            out.write(toHexBytes(digester.digest.digest()));
         } finally {
            if (out != null) out.close();
         }

         return writeTo;
      }

      EnumSet<KotlinLibrary> candidateDownloads() {
         EnumSet<KotlinLibrary> willDownload = EnumSet.noneOf(KotlinLibrary.class);
         willDownload.addAll(libs);
         willDownload.removeAll(bundledElements.keySet());
         return willDownload;
      }

      void addVersion(int[] newVersion) {
         if (KotlinVersion.compareVersion(version, newVersion) < 0) {
            version = newVersion;
            bundledElements.clear();
         }
      }

      void collectKotlinMods() {
         for (String modsDir : modsDirs) {
            File dir = new File(Launch.minecraftHome, modsDir);
            for (File mod : orEmpty(dir.listFiles(jarFilter))) {
               collectKotlinInMod(mod);
            }
         }
      }

      void collectKotlinInMod(File mod) {
         try {
            JarFile jarFile = new JarFile(mod);
            shadowedKotlinInMod(mod, jarFile);
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) return;
            Attributes root = manifest.getMainAttributes();
            String mfVersion = root.getValue("MCKT-MF-Version");
            if (mfVersion == null) return;

            Integer mfVersionIn = parseInteger(mfVersion);
            String ktVersion = root.getValue("MCKT-KT-Version");
            String parts = root.getValue("MCKT-KT-Parts");
            String jars = root.getValue("MCKT-KT-Jars");

            if (mfVersionIn == null)
               throw new IllegalStateException("Unsupported MCKT manifest version: " + mfVersion);
            if (mfVersionIn > MANIFEST_VERSION)
               throw new IllegalStateException("Unsupported MCKT manifest version: " + mfVersion);
            if (ktVersion == null)
               throw new IllegalStateException("invalid MCKT manifest: MCKT-KT-Version is not specified");

            int[] version = KotlinVersion.parse(ktVersion);
            addVersion(version);

            if (version == null)
               throw new IllegalStateException("invalid MCKT manifest: MCKT-KT-Version is not parsable: " + ktVersion);

            log("MCKT Manifest Kotlin version " + ktVersion + " found in " + mod.getName());

            if (parts == null) {
               log("MCKT Manifest without parts found in " + mod.getName());
               this.libs.addAll(defaultLibs);
            } else {
               URI jarUri = mod.toURI();
               String[] partList = parts.split(",");
               String[] jarList = jars == null ? new String[partList.length] : jars.split(",");

               if (jarList.length != partList.length)
                  throw new IllegalStateException("invalid MCKT manifest: MCKT-KT-Jars and MCKT-KT-Parts conflict.");

               for (int i = 0; i < partList.length; i++) {
                  String part = partList[i].trim();
                  String jar = jarList[i];

                  KotlinLibrary lib = KotlinLibrary.byName.get(part);
                  if (lib == null) throw new IllegalStateException("unknown library key: " + part);

                  log("MCKT Manifest " + lib.libName + " found in " + mod.getName());
                  this.libs.add(lib);
                  if (jar != null) bundledElements.put(lib, URI.create("jar:" + jarUri + "!/" + jar).toURL());
               }
            }
         } catch (IOException ignored) {
         }
      }

      void shadowedKotlinInMod(File mod, JarFile jar) {
         boolean hasKotlinStdlib = false;
         for (KotlinLibrary value : KotlinLibrary.VALUES) {
            if (jar.getEntry(value.detectClassFileName) == null) continue;

            if (value == KotlinLibrary.KotlinStdlib)
               hasKotlinStdlib = true;
            log("Shadowed " + value.libName + " found in " + mod.getName());
            this.libs.add(value);
         }

         String kotlinVersion = null;
         if (hasKotlinStdlib) {
            kotlinVersion = detectKotlinVersion(jar);
         }

         if (kotlinVersion != null) {
            log("Shadowed Kotlin version " + kotlinVersion + " found in " + mod.getName());
            addVersion(KotlinVersion.parseVersionNullable(kotlinVersion));
         }
      }

      String detectKotlinVersion(JarFile jar) {
         String version = "1.1.0";
         SingleJarClassLoader loader = new SingleJarClassLoader();
         loader.jar = jar;
         try {
            Class<?> clz = loader.loadClass(kotlinVersionClassName);
            Field field = clz.getField("CURRENT");
            Object currentVersion = field.get(null);
            version = currentVersion.toString();
         } catch (ClassNotFoundException ignored) {
         } catch (NoSuchFieldException ignored) {
         } catch (IllegalAccessException ignored) {
         }
         loader.jar = null;
         return version;
      }

      File[] orEmpty(File[] files) {
         return files == null ? new File[0] : files;
      }

      Integer parseInteger(String str) {
         try {
            return Integer.parseInt(str);
         } catch (NumberFormatException ignored) {
            return null;
         }
      }

      static final LaunchClassLoader classLoader;
      static final URLClassLoader parentLoader;
      static final String[] modsDirs = new String[]{"mods", "mods" + File.separatorChar + ForgeVersion.mcVersion};
      static final String kotlinVersionClassName = q("<kotlin.KotlinVersion>");
      static final EnumSet<KotlinLibrary> defaultLibs;

      static final FilenameFilter jarFilter = new FilenameFilter() {
         @Override
         public boolean accept(File _1, String name) {
            return name.endsWith(".jar") || name.endsWith(".zip");
         }
      };

      static {
         if (Launch.classLoader == null)
            throw new IllegalStateException("LaunchClassLoader not loaded.");
         ClassLoader lclClassLoader = Launch.classLoader.getClass().getClassLoader();
         if (!(lclClassLoader instanceof URLClassLoader))
            throw new IllegalStateException("LaunchClassLoader is not loaded by URLClassLoader");
         classLoader = Launch.classLoader;
         parentLoader = (URLClassLoader) lclClassLoader;
         if (System.getProperty("java.specification.version").equals("1.8")) {
            defaultLibs = EnumSet.of(KotlinLibrary.KotlinStdlib, KotlinLibrary.KotlinStdlibJdk7, KotlinLibrary.KotlinStdlibJdk8);
         } else if (System.getProperty("java.specification.version").equals("1.7")) {
            defaultLibs = EnumSet.of(KotlinLibrary.KotlinStdlib, KotlinLibrary.KotlinStdlibJdk7);
         } else {
            defaultLibs = EnumSet.of(KotlinLibrary.KotlinStdlib);
         }
      }

      public static void staticInit() {
      }
   }

   static class SingleJarClassLoader extends ClassLoader {
      JarFile jar;

      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
         JarFile jar = this.jar;
         if (jar == null) throw new ClassNotFoundException(name);
         JarEntry entry = jar.getJarEntry(name.replace('.', '/') + ".class");
         if (entry == null) return super.findClass(name);
         ByteArrayOutputStream stream = new ByteArrayOutputStream();

         try {
            InputStream inputStream = null;
            try {
               inputStream = new BufferedInputStream(jar.getInputStream(entry));
               copy(inputStream, stream);
            } finally {
               if (inputStream != null)
                  inputStream.close();
            }
         } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
         }

         return defineClass(name, stream.toByteArray(), 0, stream.size());
      }

      public static void staticInit() {
      }
   }

   static final String resolverVersionPropName = q("<com.anatawa12.minecraft-kotlin-resolver.resolver-version>");
   static final String resolverNamePropName = q("<com.anatawa12.minecraft-kotlin-resolver.resolver-name>");
   static final int MANIFEST_VERSION = 1;
   static final double RESOLVER_VERSION = 1.0;

   static MessageDigest getSha1() {
      try {
         return MessageDigest.getInstance("SHA-1");
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
      }
   }

   static final char[] hexElements = new char[]{
      '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
   };

   static byte[] toHexBytes(byte[] digest) {
      byte[] chars = new byte[digest.length * 2];
      for (int i = 0; i < digest.length; i++) {
         chars[i * 2] = (byte) hexElements[digest[i] >>> 2 & 0x0F];
         chars[i * 2 + 1] = (byte) hexElements[digest[i] & 0xF];
      }
      return chars;
   }

   static final byte[] buf = new byte[1 << 10];

   static void copy(InputStream in, OutputStream... outs) throws IOException {
      int cnt;
      while ((cnt = in.read(buf)) != -1) {
         for (OutputStream out : outs)
            out.write(buf, 0, cnt);
      }
   }

   static String q(String s) {
      return s.substring(1, s.length() - 1);
   }

   interface KotlinCacheFinder {
      File find(KotlinLibrary library, String kotlinVersion);
   }

   static class GradleKotlinCacheFinder implements KotlinCacheFinder {
      @Override
      public File find(KotlinLibrary library, String kotlinVersion) {
         File file = new File(System.getProperty("user.home"), cacheDir + library.dottedArchitectPath(kotlinVersion));
         File[] files = file.listFiles();
         if (files == null) return null;
         for (File listFile : files) {
            File jarFile = new File(listFile, library.architectName + '-' + kotlinVersion + ".jar");
            if (jarFile.isFile())
               return jarFile;
         }
         return null;
      }

      static String cacheDir = q("<.gradle/caches/modules-2/files-2.1/>");

      public static void staticInit() {
      }
   }

   static class MavenKotlinCacheFinder implements KotlinCacheFinder {
      @Override
      public File find(KotlinLibrary library, String kotlinVersion) {
         File jarFile = new File(System.getProperty("user.home"),
            cacheDir + library.groupId.replace('.', '/')
               + '/' + library.architectName + '/' + kotlinVersion + '/' + library + '-' + kotlinVersion + ".jar");
         if (jarFile.isFile())
            return jarFile;
         return null;
      }

      static String cacheDir = q("<.m2/repository/>");

      public static void staticInit() {
      }
   }

   static class MCKTKotlinCacheFinder implements KotlinCacheFinder {
      @Override
      public File find(KotlinLibrary library, String kotlinVersion) {
         File jarFile = getCacheFile(library, kotlinVersion);
         if (!jarFile.isFile()) return null;

         InputStream in = null;
         DigesterOutputStream out = null;
         byte[] hash;
         try {
            try {
               in = new BufferedInputStream(new FileInputStream(jarFile));
               out = new DigesterOutputStream(getSha1());
               copy(in, out);
               in.close();
               in = new BufferedInputStream(new FileInputStream(jarFile + ".sha1"));
               hash = new byte[20];
               if (in.read(hash) != 20) return null;
            } finally {
               if (out != null) {
                  out.close();
               }
               if (in != null) in.close();
            }
         } catch (IOException e) {
            return null;
         }
         if (!Arrays.equals(toHexBytes(out.digest.digest()), hash))
            return null;

         return jarFile;
      }

      static File getCacheFile(KotlinLibrary packageName, String versionName) {
         return new File(System.getProperty("user.home"), cacheDir + packageName + '-' + versionName + ".jar");
      }

      static String cacheDir = q("<.cache/anatawa12-mckt-resolver/kotlin-stdlib/>");

      public static void staticInit() {
      }
   }

   static class DigesterOutputStream extends OutputStream {
      final MessageDigest digest;

      DigesterOutputStream(MessageDigest digest) {
         this.digest = digest;
      }

      @Override
      public void write(int b) {
         digest.update((byte) b);
      }

      @Override
      public void write(byte[] b, int off, int len) {
         digest.update(b, off, len);
      }

      @Override
      public void close() {
      }

      public static void staticInit() {
      }
   }

   enum KotlinLibrary {
      KotlinStdlib(q("<kotlin-stdlib>"), q("<kotlin/Pair.class>"), q("<org.jetbrains.kotlin>")),
      KotlinStdlibJdk7(q("<kotlin-stdlib-jdk7>"), q("<kotlin/internal/jdk7/JDK7PlatformImplementations.class>"), q("<org.jetbrains.kotlin>")),
      KotlinStdlibJdk8(q("<kotlin-stdlib-jdk8>"), q("<kotlin/internal/jdk8/JDK8PlatformImplementations.class>"), q("<org.jetbrains.kotlin>")),
      KotlinStdlibCommon(q("<kotlin-stdlib-common>"), q("<kotlin/Pair.kotlin_metadata>"), q("<org.jetbrains.kotlin>")),
      KotlinReflect(q("<kotlin-reflect>"), q("<kotlin/reflect/ReflectJvmMapping.class>"), q("<org.jetbrains.kotlin>")),
      JetbrainsAnnotation(q("<annotations>"), q("<org/jetbrains/annotations/Nullable.class>"), q("<org.jetbrains>"), q("<annotations>")) {
         @Override
         String getVersionOf(String version) {
            return "13.0";
         }
      },
      ;

      final String libName;
      final String detectClassFileName;
      final String groupId;
      final String architectName;

      KotlinLibrary(String name, String className, String groupId) {
         libName = name;
         detectClassFileName = className;
         this.groupId = groupId;
         this.architectName = name;
      }

      KotlinLibrary(String name, String className, String groupId, String architectName) {
         libName = name;
         detectClassFileName = className;
         this.groupId = groupId;
         this.architectName = architectName;
      }

      String architectPath(String version) {
         return groupId.replace('.', '/') + '/' + architectName + '/' + getVersionOf(version)
            + '/' + architectFileName(version);
      }

      String dottedArchitectPath(String version) {
         return groupId + '/' + architectName + '/' + architectFileName(version);
      }

      String architectFileName(String version) {
         return architectName + '-' + getVersionOf(version) + ".jar";
      }

      String getVersionOf(String version) {
         return version;
      }


      static KotlinLibrary[] VALUES = KotlinLibrary.values();
      static Map<String, KotlinLibrary> byName = new HashMap<String, KotlinLibrary>();

      static {
         for (KotlinLibrary value : VALUES) {
            byName.put(value.libName, value);
         }
      }

      public static void staticInit() {
      }
   }

   static class KotlinVersion {
      static int[] parse(String inString) {
         String[] elements = inString.split("\\.");
         try {
            if (elements.length == 3) {
               return new int[]{
                  Integer.parseInt(elements[0]),
                  Integer.parseInt(elements[1]),
                  Integer.parseInt(elements[2]),
               };
            } else if (elements.length == 2) {
               return new int[]{
                  Integer.parseInt(elements[0]),
                  Integer.parseInt(elements[1]),
                  0,
               };
            } else {
               return null;
            }
         } catch (NumberFormatException e) {
            return null;
         }
      }

      static int[] parseVersionNullable(String inString) {
         if (inString == null) return null;
         int[] version = parse(inString);
         if (version == null) throw new IllegalArgumentException("invalid version: " + inString);
         return version;
      }

      static int compareVersion(int[] a, int[] b) {
         if (a == b) return 0;
         if (a == null) return -1;
         if (b == null) return 1;
         if (a[0] < b[0]) return -1;
         if (a[0] > b[0]) return 1;
         if (a[1] < b[1]) return -1;
         if (a[1] > b[1]) return 1;
         if (a[2] < b[2]) return -1;
         if (a[2] > b[2]) return 1;
         return 0;
      }

      public static void staticInit() {
      }
   }

   static void log(String msg) {
      System.err.println(msg);
   }
}
