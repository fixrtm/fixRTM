# RTM, FixRTMを更新するときにすること

1. `./gradlew prepareMods`を実行
1. `./apply_patch.sh`を実行してパッチを適用する
1. NGTLib/RTM内における`javax/script/ScriptEngine`の使用箇所を調べて`FIXScriptEngine`で実装していないところを確認する
