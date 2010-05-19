/*   
 * Copyright 2010 John R. Hicks
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.determinato.feeddroid.util;

import android.view.KeyEvent;

/**
 * Keypad utility class
 * @author John R. Hicks <john@determinato.com>
 *
 */
public class KeyUtils {
	private KeyUtils() {}
	
	public static int intrepretDirection(int code) {
		switch(code) {
		case KeyEvent.KEYCODE_LEFT_BRACKET:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_7:
			return KeyEvent.KEYCODE_DPAD_LEFT;
			
		case KeyEvent.KEYCODE_RIGHT_BRACKET:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_9:
			return KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		
		return code;
	}
}
