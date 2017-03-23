/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.plugins.setLang( 'a11yhelp', 'zh', {
	title: '辅助工具指南',
	contents: '说明内容。若要关闭此对话框请按「ESC」。',
	legend: [
		{
		name: '一般',
		items: [
			{
			name: '编辑器工具列',
			legend: '请按 ${toolbarFocus} 以导览到工具列。利用 TAB 或 SHIFT+TAB 以便移动到下一个及前一个工具列群組。利用右方向键或左方向键以便移动到下一个及上一个工具列按钮。按下空白键或 ENTER 键启用工具列按钮。'
		},

			{
			name: '编辑器对话框',
			legend:
				'在对话框中，按下 TAB 键以导览到下一个对话框元素，按下 SHIFT+TAB 以移动到上一个对话框元素，按下 ENTER 以提交对话框，按下 ESC 以取消对话框。当对话框有多个分页时，可以使用 ALT+F10 或是在对话框分页程序中的一部份按下 TAB 以使用分页列表。焦点在分页列表上时，分别使用右方向键及左方向键移动到下一个及上一个分页。'
		},

			{
			name: '编辑器内容功能表',
			legend: '请按下「${contextMenu}」或是「应用程式键」以开启内容选单。以「TAB」或是「↓」键移动到下一个选单选项。以「SHIFT + TAB」或是「↑」键移动到上一個选单选项。按下「空白键」或是「ENTER」键以选取选单选项。以「空白键」或「ENTER」或「→」开启目前选项的子选单。以「ESC」或「←」回到父选单。以「ESC」键关闭内容菜单」。'
		},

			{
			name: '编辑器清单方块',
			legend: '在清单方块中，使用 TAB 或下方向键移动到下一个列表项目。使用 SHIFT+TAB 或上方向键移动到上一个列表项目。按下空白键或回车键以提取列表选项。按下ESC以关闭清单方块。'
		},

			{
			name: '编辑器元件路径工具栏',
			legend: '请按 ${elementsPathFocus} 以浏览元素路径列。利用TAB或右方向键以便移动到下一个元素按钮。利用SHIFT或左方向键以便移动到上一个按钮。按下空白键或回车键键来提取在编辑器中的元素。'
		}
		]
	},
		{
		name: '命令',
		items: [
			{
			name: '复原命令',
			legend: '请按下「${undo}」'
		},
			{
			name: '重复命令',
			legend: '请按下「 ${redo}」'
		},
			{
			name: '粗体命令',
			legend: '请按下「${bold}」'
		},
			{
			name: '斜体',
			legend: '请按下「${italic}」'
		},
			{
			name: '底线命令',
			legend: '请按下「${underline}」'
		},
			{
			name: '链接',
			legend: '请按下「${link}」'
		},
			{
			name: '隐藏工具列',
			legend: '请按下「${toolbarCollapse}」'
		},
			{
			name: '存取前一个焦点空间命令',
			legend: '请按下 ${accessPreviousSpace} 以存取最近但无法靠近之插字符号前的焦点空间。举例：二个相邻的HR元素。\r\n重复按键以存取较远的焦点空间。'
		},
			{
			name: '存取下一个焦点空间命令',
			legend: '请按下 ${accessNextSpace} 以存取最近但无法靠近之插字符号后的焦点空间。举例：二个相邻的HR元素。\r\n重复按键以存取较远的焦点空间。'
		},
			{
			name: '协助工具说明',
			legend: '请按下「${a11yHelp}」'
		}
		]
	}
	],
	backspace: '退格键',
	tab: 'Tab',
	enter: 'Enter',
	shift: 'Shift',
	ctrl: 'Ctrl',
	alt: 'Alt',
	pause: 'Pause',
	capslock: 'Caps Lock',
	escape: 'Esc',
	pageUp: 'Page Up',
	pageDown: 'Page Down',
	end: 'End',
	home: 'Home',
	leftArrow: '向左键号',
	upArrow: '向上键号',
	rightArrow: '向右键号',
	downArrow: '向下键号',
	insert: '插入',
	'delete': '删除',
	leftWindowKey: '左方 Windows 键',
	rightWindowKey: '右方 Windows 键',
	selectKey: '选择键',
	numpad0: 'Numpad 0',
	numpad1: 'Numpad 1',
	numpad2: 'Numpad 2',
	numpad3: 'Numpad 3',
	numpad4: 'Numpad 4',
	numpad5: 'Numpad 5',
	numpad6: 'Numpad 6',
	numpad7: 'Numpad 7',
	numpad8: 'Numpad 8',
	numpad9: 'Numpad 9',
	multiply: '乘号',
	add: '新增',
	subtract: '减号',
	decimalPoint: '小数点',
	divide: '除号',
	f1: 'F1',
	f2: 'F2',
	f3: 'F3',
	f4: 'F4',
	f5: 'F5',
	f6: 'F6',
	f7: 'F7',
	f8: 'F8',
	f9: 'F9',
	f10: 'F10',
	f11: 'F11',
	f12: 'F12',
	numLock: 'Num Lock',
	scrollLock: 'Scroll Lock',
	semiColon: '分号',
	equalSign: '等号',
	comma: '逗号',
	dash: '虚线',
	period: '句号',
	forwardSlash: '斜线',
	graveAccent: '抑音符号',
	openBracket: '左方括号',
	backSlash: '反斜线',
	closeBracket: '右方括号',
	singleQuote: '单引号'
} );
