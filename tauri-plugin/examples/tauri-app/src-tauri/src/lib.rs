#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        // Safe to register unconditionally: on non-Android targets every
        // command just resolves to Error::UnsupportedPlatform instead of
        // doing anything (see src/desktop.rs in the plugin crate). If you'd
        // rather not register a no-op plugin on desktop at all, wrap this in
        // `#[cfg(target_os = "android")]` instead.
        .plugin(tauri_plugin_silence_of_salah_engine::init())
        .run(tauri::generate_context!())
        .expect("error while running the silence-of-salah-engine example app");
}
