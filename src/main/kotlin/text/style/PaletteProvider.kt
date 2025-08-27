package text.style

object PaletteProvider {
    private var _set = false
    var palette: Palette = DefaultPalette
        set(value) {
            if (_set) {
                throw IllegalStateException("Palette can only be set once")
            }
            _set = true
            field = value
        }
}