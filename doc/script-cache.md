# javascriptのキャッシュ

## キャッシュの種類

- Script class cache
> コンパイル済javascript
>
> 通常のhash-basedではなく.classをベタ置きする
- Executed Environment
> TODO: 実装
> evalでやる実行をしたあとの状態。 ModelPackBased
>
> key: スクリプトpath

### Executed Environment

ロードしているすくりぷとが変更されていない必要があるため、
キャッシュデータにロードしているスクリプトのmodelpack name/ResourceLoation+sha1を
保存する必要がある
modelpack nameの場合, RTMの更新のたびにほぼ確実に再生成になる
後者の場合、キャッシュの検証に時間がかかるがRTMの更新などのたびに実行する必要はない
