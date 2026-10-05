(() => {
	const button = document.getElementById('theme-toggle');
	if (!button) return;

	const storageKey = 'servlet-session-cookies-theme';
	const applyTheme = (theme, save = false) => {
		const isDark = theme === 'dark';
		document.documentElement.dataset.theme = isDark ? 'dark' : 'light';
		button.setAttribute('aria-pressed', String(isDark));
		button.setAttribute('aria-label', isDark ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro');
		button.querySelector('.theme-icon').textContent = isDark ? '☀' : '☾';
		button.querySelector('.theme-label').textContent = isDark ? 'Modo claro' : 'Modo oscuro';

		if (save) {
			try {
				localStorage.setItem(storageKey, isDark ? 'dark' : 'light');
			} catch (_) {
				// El cambio de tema sigue funcionando aunque el navegador bloquee el almacenamiento.
			}
		}
	};

	let initialTheme = 'light';
	try {
		if (localStorage.getItem(storageKey) === 'dark') initialTheme = 'dark';
	} catch (_) {
		// Sin almacenamiento disponible, se mantiene el modo claro predeterminado.
	}
	applyTheme(initialTheme);

	button.addEventListener('click', () => {
		const nextTheme = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
		applyTheme(nextTheme, true);
	});
})();
