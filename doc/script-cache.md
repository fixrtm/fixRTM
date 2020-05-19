# javascriptのキャッシュ

## キャッシュの種類

- Script class cache
> コンパイル済javascript
>
> 通常のhash-basedではなく.classをベタ置きする
- Executed Environment
> evalでやる実行をしたあとの状態。 ModelPackBased
>
> key: スクリプトpath

### Executed Environment

ロードしているスクリプトが変更されていない必要があるため、
キャッシュデータにロードしているスクリプトのResourceLoation+sha1を保存している
