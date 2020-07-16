@echo off

if [%1] == [help] (
  echo  w $V:   set gradle wrapper
  echo  fixgit: fix permission flag on git index for required files
  echo  svgo:   optimize SVG icons with SGVO. SVGO must be present, type 'npm install -g svgo' if needed)
)

if [%1] == [w] (
  gradle wrapper --gradle-version=%2 --no-daemon
)
if [%1] == [fixgit] (
  echo git update-index --chmod=+x gradlew
  git update-index --chmod=+x gradlew
)
if [%1] == [svgo] (
  svgo --folder=resources/icons/ --multipass --config=svgo.yml
)
