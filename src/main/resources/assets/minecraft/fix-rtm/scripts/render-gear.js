/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

var renderClass = "jp.ngt.rtm.render.MechanismPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.ngtlib.math);
importPackage(Packages.jp.ngt.rtm.render);

function init(par1, par2) {
    var modelName = par1.getConfig().getName();
    main = renderer.registerParts(new Parts("__"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    if (pass == 0) {
        var rotation = renderer.getRotation(entity, Axis.POSITIVE_Y);
        GL11.glRotatef(rotation, 0.0, 1.0, 0.0);
        main.render(renderer);
    }

    GL11.glPopMatrix();
}
