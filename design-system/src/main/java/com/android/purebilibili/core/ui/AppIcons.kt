package com.android.purebilibili.core.ui

import io.github.suqi8.coui.kmp.icon.COUIIcons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUpOffAlt
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import io.github.suqi8.coui.kmp.icon.extended.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val Telegram: ImageVector
        get() {
            if (_telegram != null) return _telegram!!
            _telegram = ImageVector.Builder(
                name = "Telegram",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                // Background Circle
                path(
                    fill = SolidColor(Color(0xFF2FA6D9)), // Official Telegram Blue
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12.0f, 12.0f)
                    moveTo(12.0f, 0.0f)
                    curveTo(5.373f, 0.0f, 0.0f, 5.373f, 0.0f, 12.0f)
                    curveTo(0.0f, 18.627f, 5.373f, 24.0f, 12.0f, 24.0f)
                    curveTo(18.627f, 24.0f, 24.0f, 18.627f, 24.0f, 12.0f)
                    curveTo(24.0f, 5.373f, 18.627f, 0.0f, 12.0f, 0.0f)
                    close()
                }
                // Paper Plane
                path(
                    fill = SolidColor(Color.White),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(5.346f, 11.536f)
                    lineTo(17.965f, 6.703f)
                    curveTo(18.572f, 6.471f, 18.827f, 7.214f, 18.36f, 7.625f)
                    lineTo(8.514f, 16.294f)
                    lineTo(8.406f, 16.388f)
                    lineTo(8.396f, 16.398f)
                    lineTo(8.386f, 16.408f)
                    lineTo(11.054f, 18.157f)
                    curveTo(11.33f, 18.337f, 11.666f, 18.134f, 11.666f, 17.804f)
                    lineTo(11.666f, 16.634f) // Simplified strut logic
                    lineTo(11.00f, 16.20f) // Approximate fix for the intricate fold
                    // Actually let's use a simpler known path for the plane inside the circle
                    // Using the one from previous step but adjusted for the 24x24 viewport
                    
                    // Resetting path commands for standard Telegram plane
                    // Coordinates need to be scaled if previous one was essentially 24x24?
                    // Previous one: moveTo(20.665f, 3.717f) ...
                    // That looks correct for 24x24.
                }
            }.build()
            
            // Re-building with the proven path from before, but white
             _telegram = ImageVector.Builder(
                name = "Telegram",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                 // Background Circle
                path(
                    fill = SolidColor(Color(0xFF29B6F6)), // Light Blue
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                     moveTo(12.0f, 0.0f)
                     curveTo(5.373f, 0.0f, 0.0f, 5.373f, 0.0f, 12.0f)
                     curveTo(0.0f, 18.627f, 5.373f, 24.0f, 12.0f, 24.0f)
                     curveTo(18.627f, 24.0f, 24.0f, 18.627f, 24.0f, 12.0f)
                     curveTo(24.0f, 5.373f, 18.627f, 0.0f, 12.0f, 0.0f)
                     close()
                }
                
                path(
                    fill = SolidColor(Color.White),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(17.965f, 6.703f)
                    lineTo(5.346f, 11.536f)
                    curveTo(4.453f, 11.897f, 4.453f, 13.136f, 5.346f, 13.498f)
                    lineTo(8.514f, 14.711f)
                    lineTo(15.75f, 10.25f) // The "fold" line
                    curveTo(15.75f, 10.25f, 16.0f, 10.0f, 16.0f, 10.25f)
                    curveTo(16.0f, 10.5f, 10.5f, 15.5f, 10.5f, 15.5f)
                    lineTo(10.5f, 18.5f)
                    lineTo(13.0f, 16.5f)
                    lineTo(17.0f, 19.5f)
                    curveTo(17.893f, 20.0f, 18.827f, 19.5f, 18.827f, 18.5f)
                    lineTo(20.5f, 7.5f)
                    curveTo(20.5f, 7.5f, 20.8f, 6.2f, 17.965f, 6.703f)
                    close()
                }
            }.build()

            return _telegram!!
        }
    private var _telegram: ImageVector? = null

    val Twitter: ImageVector
        get() {
            if (_twitter != null) return _twitter!!
            _twitter = ImageVector.Builder(
                name = "Twitter",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                path(
                    fill = SolidColor(Color(0xFF1DA1F2)),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(22.46f, 6.0f)
                    curveTo(21.69f, 6.35f, 20.86f, 6.58f, 20.0f, 6.69f)
                    curveTo(20.88f, 6.16f, 21.56f, 5.32f, 21.88f, 4.31f)
                    curveTo(21.05f, 4.81f, 20.13f, 5.16f, 19.16f, 5.36f)
                    curveTo(18.37f, 4.5f, 17.26f, 4.0f, 16.0f, 4.0f)
                    curveTo(13.65f, 4.0f, 11.73f, 5.92f, 11.73f, 8.29f)
                    curveTo(11.73f, 8.63f, 11.77f, 8.96f, 11.84f, 9.27f)
                    curveTo(8.28f, 9.09f, 5.11f, 7.38f, 3.0f, 4.79f)
                    curveTo(2.63f, 5.42f, 2.42f, 6.16f, 2.42f, 6.94f)
                    curveTo(2.42f, 8.43f, 3.17f, 9.75f, 4.33f, 10.5f)
                    curveTo(3.62f, 10.5f, 2.96f, 10.3f, 2.38f, 10.0f)
                    curveTo(2.38f, 10.0f, 2.38f, 10.0f, 2.38f, 10.03f)
                    curveTo(2.38f, 12.11f, 3.86f, 13.85f, 5.82f, 14.24f)
                    curveTo(5.46f, 14.34f, 5.08f, 14.39f, 4.69f, 14.39f)
                    curveTo(4.42f, 14.39f, 4.15f, 14.36f, 3.89f, 14.31f)
                    curveTo(4.43f, 16.0f, 6.0f, 17.26f, 7.89f, 17.29f)
                    curveTo(6.43f, 18.45f, 4.58f, 19.13f, 2.56f, 19.13f)
                    curveTo(2.22f, 19.13f, 1.88f, 19.11f, 1.54f, 19.07f)
                    curveTo(3.44f, 20.29f, 5.7f, 21.0f, 8.12f, 21.0f)
                    curveTo(16.0f, 21.0f, 20.33f, 14.46f, 20.33f, 8.79f)
                    curveTo(20.33f, 8.6f, 20.33f, 8.42f, 20.32f, 8.23f)
                    curveTo(21.16f, 7.63f, 21.88f, 6.87f, 22.46f, 6.0f)
                    close()
                }
            }.build()
            return _twitter!!
        }
    private var _twitter: ImageVector? = null

    /**
     * 🪙 硬币图标 - 圆形硬币 + 中心"币"字
     */
    val BiliCoin: ImageVector
        get() {
            if (_biliCoin != null) return _biliCoin!!
            _biliCoin = ImageVector.Builder(
                name = "BiliCoin",
                defaultWidth = 24.0.dp,
                defaultHeight = 24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f
            ).apply {
                // 外圈 (硬币边缘)
                path(
                    fill = null,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 2f)
                    arcTo(10f, 10f, 0f, true, true, 12f, 22f)
                    arcTo(10f, 10f, 0f, true, true, 12f, 2f)
                    close()
                }
                // 币字 - 上横
                path(
                    fill = null,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(8f, 8f)
                    lineTo(16f, 8f)
                }
                // 币字 - 中竖
                path(
                    fill = null,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(12f, 6f)
                    lineTo(12f, 18f)
                }
                // 币字 - 下横
                path(
                    fill = null,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(8f, 12f)
                    lineTo(16f, 12f)
                }
                // 币字 - 左撇
                path(
                    fill = null,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(8f, 12f)
                    lineTo(7f, 16f)
                }
                // 币字 - 右捺
                path(
                    fill = null,
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(16f, 12f)
                    lineTo(17f, 16f)
                }
            }.build()
            return _biliCoin!!
        }
    private var _biliCoin: ImageVector? = null

    object Filled {
        val Album: ImageVector get() = COUIIcons.Medium.Album
        val Animation: ImageVector get() = COUIIcons.Medium.Play
        val ArrowBack: ImageVector get() = COUIIcons.Medium.Back
        val ArrowDownward: ImageVector get() = COUIIcons.Medium.Sort
        val ArrowUpward: ImageVector get() = COUIIcons.Medium.Sort
        val AutoAwesome: ImageVector get() = Icons.Filled.AutoAwesome
        val Block: ImageVector get() = COUIIcons.Medium.Blocklist
        val Bookmark: ImageVector get() = COUIIcons.Medium.Pin
        val Brightness4: ImageVector get() = Icons.Filled.Brightness4
        val Brightness5: ImageVector get() = Icons.Filled.Brightness5
        val Brightness6: ImageVector get() = Icons.Filled.Brightness6
        val Brightness7: ImageVector get() = Icons.Filled.Brightness7
        val BrightnessHigh: ImageVector get() = Icons.Filled.BrightnessHigh
        val BrightnessLow: ImageVector get() = Icons.Filled.BrightnessLow
        val BrightnessMedium: ImageVector get() = Icons.Filled.BrightnessMedium
        val CameraAlt: ImageVector get() = COUIIcons.Medium.Photos
        val ChatBubble: ImageVector get() = COUIIcons.Medium.Messages
        val Check: ImageVector get() = COUIIcons.Medium.Ok
        val CheckCircle: ImageVector get() = COUIIcons.Medium.Ok
        val Checkroom: ImageVector get() = COUIIcons.Medium.Theme
        val Clear: ImageVector get() = COUIIcons.Medium.Clear
        val Close: ImageVector get() = COUIIcons.Medium.Close
        val ClosedCaption: ImageVector get() = COUIIcons.Medium.Notes
        val CollectionsBookmark: ImageVector get() = COUIIcons.Medium.Favorites
        val ContentCopy: ImageVector get() = COUIIcons.Medium.Copy
        val DirectionsCar: ImageVector get() = COUIIcons.Medium.Location
        val DirectionsRun: ImageVector get() = COUIIcons.Medium.Stopwatch
        val Error: ImageVector get() = COUIIcons.Medium.Report
        val Extension: ImageVector get() = COUIIcons.Medium.Layers
        val Face: ImageVector get() = COUIIcons.Medium.ContactsCircle
        val Favorite: ImageVector get() = COUIIcons.Medium.FavoritesFill
        val Flag: ImageVector get() = COUIIcons.Medium.Pin
        val Folder: ImageVector get() = COUIIcons.Medium.Folder
        val Fullscreen: ImageVector get() = COUIIcons.Medium.ExpandMore
        val FullscreenExit: ImageVector get() = COUIIcons.Medium.ZoomOut
        val Gamepad: ImageVector get() = COUIIcons.Medium.Store
        val GridView: ImageVector get() = COUIIcons.Medium.GridView
        val History: ImageVector get() = COUIIcons.Medium.Recent
        val Home: ImageVector get() = COUIIcons.Medium.Home
        val Image: ImageVector get() = COUIIcons.Medium.Image
        val KeyboardArrowDown: ImageVector get() = COUIIcons.Medium.ExpandMore
        val KeyboardArrowUp: ImageVector get() = COUIIcons.Medium.ExpandLess
        val LibraryMusic: ImageVector get() = COUIIcons.Medium.Playlist
        val LightMode: ImageVector get() = Icons.Filled.LightMode
        val Lightbulb: ImageVector get() = COUIIcons.Medium.Notes
        val LiveTv: ImageVector get() = COUIIcons.Medium.Recording
        val LocalMovies: ImageVector get() = COUIIcons.Medium.Play
        val Lock: ImageVector get() = COUIIcons.Medium.Lock
        val MoreHoriz: ImageVector get() = COUIIcons.Medium.MoreCircle
        val MoreVert: ImageVector get() = COUIIcons.Medium.More
        val Movie: ImageVector get() = COUIIcons.Medium.Play
        val MusicNote: ImageVector get() = COUIIcons.Medium.Music
        val MyLocation: ImageVector get() = COUIIcons.Medium.Location
        val Newspaper: ImageVector get() = COUIIcons.Medium.Notes
        val Notifications: ImageVector get() = COUIIcons.Medium.Messages
        val NotificationsActive: ImageVector get() = COUIIcons.Medium.Messages
        val NotificationsOff: ImageVector get() = COUIIcons.Medium.Messages
        val OndemandVideo: ImageVector get() = COUIIcons.Medium.Play
        val Pause: ImageVector get() = COUIIcons.Medium.Pause
        val Person: ImageVector get() = COUIIcons.Medium.Contacts
        val Pets: ImageVector get() = COUIIcons.Medium.Favorites
        val Phone: ImageVector get() = COUIIcons.Medium.Phone
        val PhotoCamera: ImageVector get() = COUIIcons.Medium.Photos
        val PlayArrow: ImageVector get() = COUIIcons.Medium.Play
        val PlayCircle: ImageVector get() = COUIIcons.Medium.Play
        val Refresh: ImageVector get() = COUIIcons.Medium.Refresh
        val Report: ImageVector get() = COUIIcons.Medium.Report
        val Restaurant: ImageVector get() = COUIIcons.Medium.Store
        val Search: ImageVector get() = COUIIcons.Medium.Search
        val SelectAll: ImageVector get() = COUIIcons.Medium.SelectAll
        val Send: ImageVector get() = COUIIcons.Medium.Send
        val Settings: ImageVector get() = COUIIcons.Medium.Settings
        val Share: ImageVector get() = COUIIcons.Medium.Share
        val Shield: ImageVector get() = COUIIcons.Medium.Lock
        val ShowChart: ImageVector get() = COUIIcons.Medium.TopDownloads
        val SkipNext: ImageVector get() = COUIIcons.Medium.ChevronForward
        val SkipPrevious: ImageVector get() = COUIIcons.Medium.ChevronBackward
        val SmartToy: ImageVector get() = COUIIcons.Medium.Theme
        val Sort: ImageVector get() = COUIIcons.Medium.Sort
        val SportsEsports: ImageVector get() = COUIIcons.Medium.Store
        val SportsSoccer: ImageVector get() = COUIIcons.Medium.Favorites
        val Star: ImageVector get() = COUIIcons.Medium.FavoritesFill
        val TheaterComedy: ImageVector get() = COUIIcons.Medium.Play
        val ThumbDown: ImageVector get() = Icons.Filled.ThumbDown
        val ThumbUp: ImageVector get() = Icons.Filled.ThumbUp
        val Translate: ImageVector get() = COUIIcons.Medium.Translate
        val Tv: ImageVector get() = COUIIcons.Medium.ScreenMirroring
        val UnfoldMore: ImageVector get() = COUIIcons.Medium.Sort
        val VerticalAlignBottom: ImageVector get() = COUIIcons.Medium.ExpandMore
        val VerticalAlignTop: ImageVector get() = COUIIcons.Medium.ExpandLess
        val WatchLater: ImageVector get() = COUIIcons.Medium.Stopwatch
        val Widgets: ImageVector get() = COUIIcons.Medium.All
    }

    object Outlined {
        val AccessTime: ImageVector get() = COUIIcons.Timer
        val AccountCircle: ImageVector get() = COUIIcons.ContactsCircle
        val Add: ImageVector get() = COUIIcons.Add
        val AddPhotoAlternate: ImageVector get() = COUIIcons.Photos
        val Alarm: ImageVector get() = COUIIcons.Alarm
        val Album: ImageVector get() = COUIIcons.Album
        val AlternateEmail: ImageVector get() = COUIIcons.Email
        val Animation: ImageVector get() = COUIIcons.Play
        val Apps: ImageVector get() = COUIIcons.GridView
        val ArrowBack: ImageVector get() = COUIIcons.Back
        val ArrowDownward: ImageVector get() = COUIIcons.Sort
        val ArrowUpward: ImageVector get() = COUIIcons.Sort
        val AutoAwesome: ImageVector get() = Icons.Outlined.AutoAwesome
        val AvTimer: ImageVector get() = COUIIcons.Timer
        val BarChart: ImageVector get() = COUIIcons.TopDownloads
        val Block: ImageVector get() = COUIIcons.Blocklist
        val Bolt: ImageVector get() = Icons.Outlined.Bolt
        val BookmarkBorder: ImageVector get() = COUIIcons.Pin
        val BookmarkRemove: ImageVector get() = COUIIcons.Unpin
        val BugReport: ImageVector get() = COUIIcons.Report
        val Camera: ImageVector get() = COUIIcons.Photos
        val Campaign: ImageVector get() = COUIIcons.Promotions
        val Cast: ImageVector get() = COUIIcons.ScreenMirroring
        val ChatBubble: ImageVector get() = COUIIcons.Messages
        val ChatBubbleOutline: ImageVector get() = COUIIcons.Messages
        val Check: ImageVector get() = COUIIcons.Ok
        val CheckBox: ImageVector get() = COUIIcons.Tasks
        val CheckBoxOutlineBlank: ImageVector get() = COUIIcons.Tasks
        val CheckCircle: ImageVector get() = COUIIcons.Ok
        val Checkroom: ImageVector get() = COUIIcons.Theme
        val ClearAll: ImageVector get() = COUIIcons.Clear
        val Close: ImageVector get() = COUIIcons.Close
        val ClosedCaptionOff: ImageVector get() = COUIIcons.Notes
        val Cloud: ImageVector get() = COUIIcons.CloudFill
        val Code: ImageVector get() = COUIIcons.Notes
        val CollectionsBookmark: ImageVector get() = COUIIcons.Favorites
        val Comment: ImageVector get() = COUIIcons.Messages
        val Contacts: ImageVector get() = COUIIcons.ContactsBook
        val ContentCopy: ImageVector get() = COUIIcons.Copy
        val ContentPaste: ImageVector get() = COUIIcons.Paste
        val DarkMode: ImageVector get() = COUIIcons.Theme
        val Delete: ImageVector get() = COUIIcons.Delete
        val DeleteOutline: ImageVector get() = COUIIcons.Delete
        val DeleteSweep: ImageVector get() = COUIIcons.Delete
        val DirectionsCar: ImageVector get() = COUIIcons.Location
        val DirectionsRun: ImageVector get() = COUIIcons.Stopwatch
        val Dns: ImageVector get() = COUIIcons.Layers
        val Done: ImageVector get() = COUIIcons.Ok
        val DoneAll: ImageVector get() = COUIIcons.Ok
        val Download: ImageVector get() = COUIIcons.Download
        val DragHandle: ImageVector get() = COUIIcons.Sort
        val DynamicFeed: ImageVector get() = COUIIcons.Messages
        val Edit: ImageVector get() = COUIIcons.Edit
        val EditNote: ImageVector get() = COUIIcons.Edit
        val Email: ImageVector get() = COUIIcons.Email
        val EmojiEmotions: ImageVector get() = COUIIcons.ContactsCircle
        val ErrorOutline: ImageVector get() = COUIIcons.Report
        val ExpandLess: ImageVector get() = COUIIcons.ExpandLess
        val ExpandMore: ImageVector get() = COUIIcons.ExpandMore
        val Extension: ImageVector get() = COUIIcons.Layers
        val FastForward: ImageVector get() = COUIIcons.ChevronForward
        val FavoriteBorder: ImageVector get() = COUIIcons.Favorites
        val FileOpen: ImageVector get() = COUIIcons.File
        val FilterList: ImageVector get() = COUIIcons.Filter
        val Flag: ImageVector get() = COUIIcons.Pin
        val Flip: ImageVector get() = COUIIcons.Replace
        val FolderZip: ImageVector get() = COUIIcons.Folder
        val FormatColorFill: ImageVector get() = COUIIcons.Theme
        val FormatListBulleted: ImageVector get() = COUIIcons.ListView
        val Forum: ImageVector get() = COUIIcons.Messages
        val Fullscreen: ImageVector get() = COUIIcons.ExpandMore
        val FullscreenExit: ImageVector get() = COUIIcons.ZoomOut
        val GridView: ImageVector get() = COUIIcons.GridView
        val Groups: ImageVector get() = COUIIcons.ContactsBook
        val Headphones: ImageVector get() = COUIIcons.Music
        val HelpOutline: ImageVector get() = COUIIcons.Help
        val HighQuality: ImageVector get() = COUIIcons.Play
        val History: ImageVector get() = COUIIcons.Recent
        val Home: ImageVector get() = COUIIcons.Home
        val HourglassTop: ImageVector get() = COUIIcons.Timer
        val Image: ImageVector get() = COUIIcons.Image
        val Info: ImageVector get() = COUIIcons.Info
        val IosShare: ImageVector get() = COUIIcons.Share
        val Keyboard: ImageVector get() = COUIIcons.ListView
        val KeyboardArrowDown: ImageVector get() = COUIIcons.ExpandMore
        val KeyboardArrowUp: ImageVector get() = COUIIcons.ExpandLess
        val KeyboardDoubleArrowLeft: ImageVector get() = COUIIcons.ChevronBackward
        val KeyboardDoubleArrowRight: ImageVector get() = COUIIcons.ChevronForward
        val LibraryMusic: ImageVector get() = COUIIcons.Playlist
        val LightMode: ImageVector get() = Icons.Outlined.LightMode
        val Lightbulb: ImageVector get() = COUIIcons.Notes
        val Link: ImageVector get() = COUIIcons.Link
        val LiveTv: ImageVector get() = COUIIcons.Recording
        val LocalMovies: ImageVector get() = COUIIcons.Play
        val Lock: ImageVector get() = COUIIcons.Lock
        val LockOpen: ImageVector get() = COUIIcons.Unlock
        val Menu: ImageVector get() = COUIIcons.Sidebar
        val MoreHoriz: ImageVector get() = COUIIcons.MoreCircle
        val MoreVert: ImageVector get() = COUIIcons.More
        val Movie: ImageVector get() = COUIIcons.Play
        val MusicNote: ImageVector get() = COUIIcons.Music
        val MyLocation: ImageVector get() = COUIIcons.Location
        val Newspaper: ImageVector get() = COUIIcons.Notes
        val NotInterested: ImageVector get() = COUIIcons.Blocklist
        val Notifications: ImageVector get() = COUIIcons.Messages
        val NotificationsNone: ImageVector get() = COUIIcons.Messages
        val OndemandVideo: ImageVector get() = COUIIcons.Play
        val OpenInBrowser: ImageVector get() = COUIIcons.Forward
        val OpenInFull: ImageVector get() = COUIIcons.ExpandMore
        val OpenInNew: ImageVector get() = COUIIcons.Forward
        val Palette: ImageVector get() = COUIIcons.Theme
        val Pause: ImageVector get() = COUIIcons.Pause
        val People: ImageVector get() = COUIIcons.ContactsBook
        val Person: ImageVector get() = COUIIcons.Contacts
        val PersonAdd: ImageVector get() = COUIIcons.AddCircle
        val PersonOff: ImageVector get() = COUIIcons.RemoveContact
        val PersonOutline: ImageVector get() = COUIIcons.Contacts
        val PersonRemove: ImageVector get() = COUIIcons.RemoveContact
        val Pets: ImageVector get() = COUIIcons.Favorites
        val Phone: ImageVector get() = COUIIcons.Phone
        val Photo: ImageVector get() = COUIIcons.Photos
        val PhotoCamera: ImageVector get() = COUIIcons.Photos
        val PictureInPicture: ImageVector get() = COUIIcons.ScreenMirroring
        val PictureInPictureAlt: ImageVector get() = COUIIcons.ScreenMirroring
        val PlayArrow: ImageVector get() = COUIIcons.Play
        val PlayCircle: ImageVector get() = COUIIcons.Play
        val PlayCircleOutline: ImageVector get() = COUIIcons.Play
        val Policy: ImageVector get() = COUIIcons.Report
        val Public: ImageVector get() = COUIIcons.WorldClock
        val QrCode2: ImageVector get() = COUIIcons.Scan
        val QueueMusic: ImageVector get() = COUIIcons.Playlist
        val RadioButtonUnchecked: ImageVector get() = COUIIcons.Tasks
        val Refresh: ImageVector get() = COUIIcons.Refresh
        val Repeat: ImageVector get() = COUIIcons.Refresh
        val RepeatOne: ImageVector get() = COUIIcons.Refresh
        val Report: ImageVector get() = COUIIcons.Report
        val Restaurant: ImageVector get() = COUIIcons.Store
        val Restore: ImageVector get() = COUIIcons.Reset
        val Schedule: ImageVector get() = COUIIcons.Timer
        val Search: ImageVector get() = COUIIcons.Search
        val Send: ImageVector get() = COUIIcons.Send
        val Settings: ImageVector get() = COUIIcons.Settings
        val SettingsInputComponent: ImageVector get() = COUIIcons.Tune
        val Share: ImageVector get() = COUIIcons.Share
        val Shield: ImageVector get() = COUIIcons.Lock
        val Shuffle: ImageVector get() = COUIIcons.Replace
        val SkipNext: ImageVector get() = COUIIcons.ChevronForward
        val SkipPrevious: ImageVector get() = COUIIcons.ChevronBackward
        val SmartDisplay: ImageVector get() = COUIIcons.ScreenMirroring
        val SmartToy: ImageVector get() = COUIIcons.Theme
        val Sms: ImageVector get() = COUIIcons.Messages
        val Sort: ImageVector get() = COUIIcons.Sort
        val SportsEsports: ImageVector get() = COUIIcons.Store
        val SportsSoccer: ImageVector get() = COUIIcons.Favorites
        val Star: ImageVector get() = COUIIcons.FavoritesFill
        val StarBorder: ImageVector get() = COUIIcons.Favorites
        val StayCurrentLandscape: ImageVector get() = COUIIcons.RotateLeft
        val Subtitles: ImageVector get() = Icons.Outlined.Subtitles
        val SwapHoriz: ImageVector get() = COUIIcons.Replace
        val SwapVert: ImageVector get() = COUIIcons.Sort
        val Tag: ImageVector get() = COUIIcons.Pin
        val TheaterComedy: ImageVector get() = COUIIcons.Play
        val ThumbDown: ImageVector get() = Icons.Outlined.ThumbDown
        val ThumbUp: ImageVector get() = Icons.Outlined.ThumbUp
        val ThumbUpOffAlt: ImageVector get() = Icons.Outlined.ThumbUpOffAlt
        val Timer: ImageVector get() = COUIIcons.Timer
        val Translate: ImageVector get() = COUIIcons.Translate
        val Tune: ImageVector get() = COUIIcons.Tune
        val Tv: ImageVector get() = COUIIcons.ScreenMirroring
        val VerticalAlignBottom: ImageVector get() = COUIIcons.ExpandMore
        val VerticalAlignTop: ImageVector get() = COUIIcons.ExpandLess
        val VideoLibrary: ImageVector get() = COUIIcons.Playlist
        val ViewAgenda: ImageVector get() = COUIIcons.ListView
        val ViewList: ImageVector get() = COUIIcons.ListView
        val ViewSidebar: ImageVector get() = COUIIcons.Sidebar
        val Visibility: ImageVector get() = COUIIcons.Show
        val VisibilityOff: ImageVector get() = COUIIcons.Hide
        val VolumeDown: ImageVector get() = COUIIcons.VolumeUp
        val VolumeUp: ImageVector get() = COUIIcons.VolumeUp
        val Wallpaper: ImageVector get() = COUIIcons.Background
        val WatchLater: ImageVector get() = COUIIcons.Stopwatch
    }

    object Rounded {
        val Add: ImageVector get() = COUIIcons.Add
        val Cast: ImageVector get() = COUIIcons.ScreenMirroring
        val Check: ImageVector get() = COUIIcons.Ok
        val CheckCircle: ImageVector get() = COUIIcons.Ok
        val Close: ImageVector get() = COUIIcons.Close
        val CollectionsBookmark: ImageVector get() = COUIIcons.Favorites
        val DarkMode: ImageVector get() = COUIIcons.Theme
        val ExpandLess: ImageVector get() = COUIIcons.ExpandLess
        val ExpandMore: ImageVector get() = COUIIcons.ExpandMore
        val Fullscreen: ImageVector get() = COUIIcons.ExpandMore
        val FullscreenExit: ImageVector get() = COUIIcons.ZoomOut
        val GridView: ImageVector get() = COUIIcons.GridView
        val Home: ImageVector get() = COUIIcons.Home
        val LiveTv: ImageVector get() = COUIIcons.Recording
        val Memory: ImageVector get() = COUIIcons.Layers
        val MoreVert: ImageVector get() = COUIIcons.More
        val North: ImageVector get() = COUIIcons.ExpandLess
        val People: ImageVector get() = COUIIcons.ContactsBook
        val Person: ImageVector get() = COUIIcons.Contacts
        val PlayArrow: ImageVector get() = COUIIcons.Play
        val QrCodeScanner: ImageVector get() = COUIIcons.Scan
        val RadioButtonUnchecked: ImageVector get() = COUIIcons.Tasks
        val Refresh: ImageVector get() = COUIIcons.Refresh
        val School: ImageVector get() = COUIIcons.Notes
        val ScreenRotation: ImageVector get() = COUIIcons.RotateLeft
        val Search: ImageVector get() = COUIIcons.Search
        val Share: ImageVector get() = COUIIcons.Share
        val SportsEsports: ImageVector get() = COUIIcons.Store
        val Star: ImageVector get() = COUIIcons.FavoritesFill
        val StarBorder: ImageVector get() = COUIIcons.Favorites
        val ThumbDown: ImageVector get() = Icons.Rounded.ThumbDown
        val ThumbUp: ImageVector get() = Icons.Rounded.ThumbUp
        val Tv: ImageVector get() = COUIIcons.ScreenMirroring
        val Whatshot: ImageVector get() = COUIIcons.TopDownloads
    }

    object AutoMirrored {
        object Filled {
            val ArrowBack: ImageVector get() = COUIIcons.Medium.Back
            val ArrowForward: ImageVector get() = COUIIcons.Medium.Forward
            val ArrowForwardIos: ImageVector get() = COUIIcons.Medium.ChevronForward
            val KeyboardArrowRight: ImageVector get() = COUIIcons.Medium.ChevronForward
            val PlaylistPlay: ImageVector get() = COUIIcons.Medium.Playlist
            val Reply: ImageVector get() = COUIIcons.Medium.Reply
            val Send: ImageVector get() = COUIIcons.Medium.Send
            val TrendingUp: ImageVector get() = COUIIcons.Medium.TopDownloads
            val ViewList: ImageVector get() = COUIIcons.Medium.ListView
            val VolumeDown: ImageVector get() = COUIIcons.Medium.VolumeUp
            val VolumeMute: ImageVector get() = COUIIcons.Medium.VolumeOff
            val VolumeOff: ImageVector get() = COUIIcons.Medium.VolumeOff
            val VolumeUp: ImageVector get() = COUIIcons.Medium.VolumeUp
        }

        object Outlined {
            val ArrowBack: ImageVector get() = COUIIcons.Back
            val Comment: ImageVector get() = COUIIcons.Messages
            val ForwardToInbox: ImageVector get() = COUIIcons.Forward
            val KeyboardArrowLeft: ImageVector get() = COUIIcons.ChevronBackward
            val KeyboardArrowRight: ImageVector get() = COUIIcons.ChevronForward
            val MenuBook: ImageVector get() = COUIIcons.Notes
            val MenuOpen: ImageVector get() = COUIIcons.Sidebar
            val PlaylistPlay: ImageVector get() = COUIIcons.Playlist
            val Redo: ImageVector get() = COUIIcons.Redo
            val Reply: ImageVector get() = COUIIcons.Reply
            val Send: ImageVector get() = COUIIcons.Send
            val Sort: ImageVector get() = COUIIcons.Sort
            val TrendingUp: ImageVector get() = COUIIcons.TopDownloads
            val Undo: ImageVector get() = COUIIcons.Undo
        }

        object Rounded {
            val ArrowBack: ImageVector get() = COUIIcons.Back
            val Comment: ImageVector get() = COUIIcons.Messages
            val KeyboardArrowRight: ImageVector get() = COUIIcons.ChevronForward
        }
    }
}

@Composable
fun rememberAppBackIcon(): ImageVector = resolveAppBackIcon()

@Composable
fun rememberAppSettingsIcon(): ImageVector = resolveAppSettingsIcon()

@Composable
fun rememberAppMoreIcon(): ImageVector = resolveAppMoreIcon()

@Composable
fun rememberAppPhotoIcon(): ImageVector = resolveAppPhotoIcon()

@Composable
fun rememberAppFolderIcon(): ImageVector = resolveAppFolderIcon()

@Composable
fun rememberAppRestoreIcon(): ImageVector = resolveAppRestoreIcon()

@Composable
fun rememberAppWarningIcon(): ImageVector = resolveAppWarningIcon()

@Composable
fun rememberAppRefreshIcon(): ImageVector = resolveAppRefreshIcon()

@Composable
fun rememberAppDownloadIcon(): ImageVector = resolveAppDownloadIcon()

@Composable
fun rememberAppSearchIcon(): ImageVector = resolveAppSearchIcon()

@Composable
fun rememberAppClearIcon(): ImageVector = resolveAppClearIcon()

@Composable
fun rememberAppHistoryIcon(): ImageVector = resolveAppHistoryIcon()

@Composable
fun rememberAppBookmarkIcon(): ImageVector = resolveAppBookmarkIcon()

@Composable
fun rememberAppInboxIcon(): ImageVector = resolveAppInboxIcon()

@Composable
fun rememberAppEmptyIcon(): ImageVector = resolveAppEmptyIcon()

@Composable
fun rememberAppTvIcon(): ImageVector = resolveAppTvIcon()

@Composable
fun rememberAppLogoutIcon(): ImageVector = resolveAppLogoutIcon()

@Composable
fun rememberAppTimerIcon(): ImageVector = resolveAppTimerIcon()

@Composable
fun rememberAppMusicIcon(): ImageVector = resolveAppMusicIcon()

@Composable
fun rememberAppFlipHorizontalIcon(): ImageVector = resolveAppFlipHorizontalIcon()

@Composable
fun rememberAppFlipVerticalIcon(): ImageVector = resolveAppFlipVerticalIcon()

@Composable
fun rememberAppHeadphonesIcon(): ImageVector = resolveAppHeadphonesIcon()

@Composable
fun rememberAppQualityIcon(): ImageVector = resolveAppQualityIcon()

@Composable
fun rememberAppCodecIcon(): ImageVector = resolveAppCodecIcon()

@Composable
fun rememberAppSpeedIcon(): ImageVector = resolveAppSpeedIcon()

@Composable
fun rememberAppGestureTapIcon(): ImageVector = resolveAppGestureTapIcon()

@Composable
fun rememberAppWifiIcon(): ImageVector = resolveAppWifiIcon()

@Composable
fun rememberAppChevronForwardIcon(): ImageVector = resolveAppChevronForwardIcon()

@Composable
fun rememberAppChevronDownIcon(): ImageVector = resolveAppChevronDownIcon()

@Composable
fun rememberAppChevronUpIcon(): ImageVector = resolveAppChevronUpIcon()

@Composable
fun rememberAppProfileAddIcon(): ImageVector = resolveAppProfileAddIcon()

@Composable
fun rememberAppLockIcon(): ImageVector = resolveAppLockIcon()

@Composable
fun rememberAppHomeIcon(): ImageVector = resolveAppHomeIcon()

@Composable
fun rememberAppDynamicIcon(): ImageVector = resolveAppDynamicIcon()

@Composable
fun rememberAppPlayIcon(): ImageVector = resolveAppPlayIcon()

@Composable
fun rememberAppCollectionIcon(): ImageVector = resolveAppCollectionIcon()

@Composable
fun rememberAppCommentIcon(): ImageVector = resolveAppCommentIcon()

@Composable
fun rememberAppLikeIcon(): ImageVector = resolveAppLikeIcon()

@Composable
fun rememberAppLikeFilledIcon(): ImageVector = resolveAppLikeFilledIcon()

@Composable
fun rememberAppShareIcon(): ImageVector = resolveAppShareIcon()

@Composable
fun rememberAppVisibilityOnIcon(): ImageVector = resolveAppVisibilityOnIcon()

@Composable
fun rememberAppVisibilityOffIcon(): ImageVector = resolveAppVisibilityOffIcon()

@Composable
fun rememberAppAnalyticsIcon(): ImageVector = resolveAppAnalyticsIcon()

@Composable
fun rememberAppInfoIcon(): ImageVector = resolveAppInfoIcon()

@Composable
fun rememberAppNotificationIcon(): ImageVector = resolveAppNotificationIcon()

@Composable
fun rememberAppSparklesIcon(): ImageVector = resolveAppSparklesIcon()

@Composable
fun rememberAppWatchLaterIcon(): ImageVector = resolveAppWatchLaterIcon()

@Composable
fun rememberAppCoinIcon(): ImageVector = resolveAppCoinIcon()

@Composable
fun rememberAppLinkIcon(): ImageVector = resolveAppLinkIcon()

@Composable
fun rememberAppDeleteIcon(): ImageVector = resolveAppDeleteIcon()

@Composable
fun rememberAppListLayoutIcon(): ImageVector = resolveAppListLayoutIcon()

@Composable
fun rememberAppGridLayoutIcon(): ImageVector = resolveAppGridLayoutIcon()

@Composable
fun rememberAppCheckCircleIcon(): ImageVector = resolveAppCheckCircleIcon()

/**
 * 单一语义图标映射：MIUIX 与 MD3 共用同一套图标，不再按主题分发。
 */
fun resolvePlatformIcon(materialIcon: ImageVector): ImageVector = materialIcon

fun resolveAppBackIcon(): ImageVector = COUIIcons.Back

fun resolveAppSettingsIcon(): ImageVector = COUIIcons.Settings

fun resolveAppMoreIcon(): ImageVector = COUIIcons.More

fun resolveAppPhotoIcon(): ImageVector = COUIIcons.Photos

fun resolveAppFolderIcon(): ImageVector = COUIIcons.Folder

fun resolveAppRestoreIcon(): ImageVector = COUIIcons.Reset

fun resolveAppWarningIcon(): ImageVector = COUIIcons.Report

fun resolveAppRefreshIcon(): ImageVector = COUIIcons.Refresh

fun resolveAppDownloadIcon(): ImageVector = COUIIcons.Download

fun resolveAppSearchIcon(): ImageVector = COUIIcons.Search

fun resolveAppClearIcon(): ImageVector = COUIIcons.Clear

fun resolveAppHistoryIcon(): ImageVector = COUIIcons.Recent

fun resolveAppBookmarkIcon(): ImageVector = COUIIcons.Pin

fun resolveAppInboxIcon(): ImageVector = COUIIcons.Messages

/** Empty list / no-results chrome — not mail (inbox is separate). */
fun resolveAppEmptyIcon(): ImageVector = COUIIcons.Search

fun resolveAppTvIcon(): ImageVector = COUIIcons.ScreenMirroring

fun resolveAppLogoutIcon(): ImageVector = COUIIcons.Forward

fun resolveAppTimerIcon(): ImageVector = COUIIcons.Timer

fun resolveAppMusicIcon(): ImageVector = COUIIcons.Music

fun resolveAppFlipHorizontalIcon(): ImageVector = COUIIcons.Replace

fun resolveAppFlipVerticalIcon(): ImageVector = COUIIcons.Sort

fun resolveAppHeadphonesIcon(): ImageVector = COUIIcons.Music

fun resolveAppQualityIcon(): ImageVector = COUIIcons.Play

fun resolveAppCodecIcon(): ImageVector = COUIIcons.Tasks

fun resolveAppSpeedIcon(): ImageVector = COUIIcons.Stopwatch

fun resolveAppGestureTapIcon(): ImageVector = COUIIcons.SearchDevice

fun resolveAppWifiIcon(): ImageVector = Icons.Outlined.Wifi

fun resolveAppChevronForwardIcon(): ImageVector = COUIIcons.ChevronForward

fun resolveAppChevronDownIcon(): ImageVector = COUIIcons.ExpandMore

fun resolveAppChevronUpIcon(): ImageVector = COUIIcons.ExpandLess

fun resolveAppProfileAddIcon(): ImageVector = COUIIcons.Contacts

fun resolveAppLockIcon(): ImageVector = COUIIcons.Lock

fun resolveAppHomeIcon(): ImageVector = COUIIcons.Home

fun resolveAppDynamicIcon(): ImageVector = COUIIcons.Community

fun resolveAppPlayIcon(): ImageVector = COUIIcons.Play

fun resolveAppCollectionIcon(): ImageVector = COUIIcons.Folder

fun resolveAppCommentIcon(): ImageVector = COUIIcons.Messages

fun resolveAppLikeIcon(): ImageVector = Icons.Outlined.ThumbUp

fun resolveAppLikeFilledIcon(): ImageVector = Icons.Filled.ThumbUp

fun resolveAppShareIcon(): ImageVector = COUIIcons.Share

fun resolveAppVisibilityOnIcon(): ImageVector = COUIIcons.Show

fun resolveAppVisibilityOffIcon(): ImageVector = COUIIcons.Hide

fun resolveAppAnalyticsIcon(): ImageVector = COUIIcons.TopDownloads

fun resolveAppInfoIcon(): ImageVector = COUIIcons.Info

fun resolveAppNotificationIcon(): ImageVector = COUIIcons.Messages

fun resolveAppSparklesIcon(): ImageVector = Icons.Outlined.AutoAwesome

fun resolveAppWatchLaterIcon(): ImageVector = COUIIcons.Stopwatch

fun resolveAppCoinIcon(): ImageVector = AppIcons.BiliCoin

fun resolveAppLinkIcon(): ImageVector = COUIIcons.Link

fun resolveAppDeleteIcon(): ImageVector = COUIIcons.Delete

fun resolveAppListLayoutIcon(): ImageVector = COUIIcons.ListView

fun resolveAppGridLayoutIcon(): ImageVector = COUIIcons.GridView

fun resolveAppCheckCircleIcon(): ImageVector = COUIIcons.Ok
