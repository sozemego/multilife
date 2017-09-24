import {notify, on} from './event-bus';
import {LOGGED_IN, LOGIN, TO_MAIN_MENU} from './events';

const login = () => {
	const name = document.getElementById('name').value.trim();
	if (!name) {
		return;
	}

	notify(LOGIN, name);
};

const destroyLogin = () => {
	document.getElementById('login').innerHTML = '';
	document.getElementById('login-container').classList.add('hidden');
};

export const createLoginUi = () => {

	const loginUi = {};

	loginUi.createLoginView = () => {
		const dom = document.getElementById('login');
		dom.innerHTML = '';

		document.getElementById('login-container').classList.remove('hidden');

		const name = document.createElement('input');
		name.setAttribute('id', 'name');
		name.setAttribute('placeholder', 'Type your name');
		dom.appendChild(name);
		name.focus();

		const button = document.createElement('button');
		button.appendChild(document.createTextNode('ENTER!'));
		button.classList.add('login-button');
		button.addEventListener('click', login);

		dom.appendChild(button);
	};

	on(LOGGED_IN, destroyLogin);
	on(TO_MAIN_MENU, loginUi.createLoginView);

	return loginUi;
};