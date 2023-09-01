# Extra Icons Change Log

## 2.0 (WIP)
* Important code rework. 
* You can now use [oshi](https://github.com/oshi/oshi) in order to get battery status. This is more configurable (please see the new settings), it seems to work everywhere, even on macOS.

## 1.26 (2023/03/31)
* Add option to enable or disable IDE's Power Saver mode based on battery level. This option is disabled by default.

## 1.25 (2022/11/26)
* Remove usage of deprecated JetBrains API, fix compatibility with future IDE releases.

## 1.24 (2022/02/12)
* Improve macOS support.

## 1.23 (2022/01/22)
* Add two new icon sets for battery levels. You can choose your preferred icons set in <i>File</i> &gt; <i>Settings...</i> &gt; <i>Appearance</i> &gt; <i>Battery Status</i>.

## 1.22 (2021/10/17)
* You can now change refresh interval without restarting IDE.
* Remove broken battery status preview in config panel.

## 1.21 (2021/10/13)
* Fix battery status not refreshing when the day is changing.

## 1.20 (2021/10/09)
* Fix: the config panel was constantly marked as modified. Changes are now correctly detected.
* Minor performance optimization: avoid unnecessary calls to battery status when multiple projects are opened (status is cached for 10s and shared between all opened projects).
* Minor performance optimization: detect OS only once at startup instead of on every battery status update.

## 1.19 (2021/06/20)
* Internal: remove usage of code deprecated in IJ 2021.1.2.

## 1.18 (2021/06/05)
* Improve Linux (ACPI) and macOS support.

## 1.17 (2021/05/24)
* Plugin now requires IDE restart on install. This is a workaround for a bug in IJ that resets plugin preferences on plugin update.

## 1.16 (2021/03/26)
* Refresh the status bar immediately on settings update.

## 1.15 (2021/03/07)
* Fix usage of a deprecated API. Will prevent possible issues with future IDE releases.

## 1.14 (2020/10/03)
* Add two new icon sets for battery levels. You can choose your preferred icons set in <i>File</i> &gt; <i>Settings...</i> &gt; <i>Appearance</i> &gt; <i>Battery Status</i>.
* MS Windows: display battery life time with number of days when greater than 23:59:59.
* Internal: rework log levels.

## 1.13 (2020/09/27)
* Internal: terminate battery info thread faster on app disposal.

## 1.12 (2020/07/18)
* Improve battery status rendering with colorized SVG icons.
* Move config panel from <i>Tools</i> to <i>Appearance</i>.

## 1.11 (2020/07/11)
* Improve battery status rendering.

## 1.10 (2020/06/16)
* Set default battery status refresh interval to 90s (vs 20s previously).

## 1.9 (2020/04/09)
* Internal: fix usage of deprecated code from JNA.

## 1.8 (2020/03/16)
* Improve compatibility with IDE version 201 EAP.

## 1.7 (2020/03/10)
* Internal: migrate project to Gradle.
* Make plugin compatible with IDE version 201 EAP.
* Upgrade minimal IDE version from 173.0 to 201 (like IJ 2020.1).

## 1.6 (2019/11/10)
* Add an alternative method to get minimal battery information on MacOS.

## 1.5 (2019/07/09)
* Fix 'Last update' time: it was not refreshed.

## 1.4 (2019/06/14)
* Fix <code>java.time.DateTimeException: Invalid value for SecondOfDay (valid values 0 - 86399): 106922</code> error.

## 1.3 (2019/05/07)
* Linux and MacOS: you can customize the command that is used to retrieve battery status.
* Windows: you can choose which battery status fields to display.

## 1.2 (2019/05/02)
* Battery Status refresh time is now configurable.
* Click on the Battery Status to refresh it immediately.

## 1.1 (2019/04/29)
* Battery Status widget is now positioned after the InsertOverwrite widget.
* rework deprecated API dependency to make plugin compatible with future IDE builds (<i>StatusBarWidget.TextPresentation.getMaxPossibleText()</i>).

## 1.0
* first release.
