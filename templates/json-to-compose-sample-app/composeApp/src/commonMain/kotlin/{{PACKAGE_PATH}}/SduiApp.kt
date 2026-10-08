package {{PACKAGE_NAME}}

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jesusdmedinac.jsontocompose.LocalBehavior
import com.jesusdmedinac.jsontocompose.LocalStateHost
import com.jesusdmedinac.jsontocompose.ToCompose
import com.jesusdmedinac.jsontocompose.behavior.Behavior
import com.jesusdmedinac.jsontocompose.state.MutableStateHost
import com.jesusdmedinac.jsontocompose.state.StateHost

@Composable
fun SduiApp() {
    val nameStateHost = remember { MutableStateHost("") }
    val greetingStateHost = remember { MutableStateHost("Welcome to Server-Driven UI!") }
    var clickCount by remember { mutableIntStateOf(0) }

    val stateHosts = remember {
        mapOf<String, StateHost<*>>(
            "name_input" to nameStateHost,
            "greeting_text" to greetingStateHost,
        )
    }

    val behaviors = remember {
        mapOf<String, Behavior>(
            "on_greet_click" to object : Behavior {
                override fun invoke() {
                    clickCount++
                    val inputName = nameStateHost.state.trim()
                    if (inputName.isNotEmpty()) {
                        greetingStateHost.onStateChange("Hello, $inputName! 👋 (Click #$clickCount)")
                    } else {
                        greetingStateHost.onStateChange("Server-Driven UI in action! 🚀 (Click #$clickCount)")
                    }
                }
            }
        )
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(
                LocalStateHost provides stateHosts,
                LocalBehavior provides behaviors,
            ) {
                SAMPLE_SDUI_JSON.ToCompose()
            }
        }
    }
}

private val SAMPLE_SDUI_JSON = """
{
  "type": "Column",
  "composeModifier": {
    "operations": [
      { "type": "FillMaxSize" },
      { "type": "Padding", "value": 24 }
    ]
  },
  "properties": {
    "type": "ColumnProps",
    "horizontalAlignment": "CenterHorizontally",
    "children": [
      {
        "type": "Text",
        "properties": {
          "type": "TextProps",
          "text": "{{PROJECT_NAME}}",
          "fontSize": 26.0,
          "fontWeight": "Bold"
        }
      },
      {
        "type": "Spacer",
        "composeModifier": {
          "operations": [
            { "type": "Height", "value": 8 }
          ]
        },
        "properties": {
          "type": "SpacerProps"
        }
      },
      {
        "type": "Text",
        "properties": {
          "type": "TextProps",
          "text": "Powered by json-to-compose",
          "fontSize": 14.0
        }
      },
      {
        "type": "Spacer",
        "composeModifier": {
          "operations": [
            { "type": "Height", "value": 24 }
          ]
        },
        "properties": {
          "type": "SpacerProps"
        }
      },
      {
        "type": "Card",
        "composeModifier": {
          "operations": [
            { "type": "FillMaxWidth" },
            { "type": "Padding", "value": 8 }
          ]
        },
        "properties": {
          "type": "CardProps",
          "child": {
            "type": "Column",
            "composeModifier": {
              "operations": [
                { "type": "Padding", "value": 20 }
              ]
            },
            "properties": {
              "type": "ColumnProps",
              "horizontalAlignment": "CenterHorizontally",
              "children": [
                {
                  "type": "Text",
                  "properties": {
                    "type": "TextProps",
                    "textStateHostName": "greeting_text",
                    "fontSize": 16.0,
                    "fontWeight": "Medium"
                  }
                },
                {
                  "type": "Spacer",
                  "composeModifier": {
                    "operations": [
                      { "type": "Height", "value": 16 }
                    ]
                  },
                  "properties": {
                    "type": "SpacerProps"
                  }
                },
                {
                  "type": "TextField",
                  "composeModifier": {
                    "operations": [
                      { "type": "FillMaxWidth" }
                    ]
                  },
                  "properties": {
                    "type": "TextFieldProps",
                    "valueStateHostName": "name_input",
                    "placeholder": {
                      "type": "Text",
                      "properties": {
                        "type": "TextProps",
                        "text": "Type your name here..."
                      }
                    }
                  }
                },
                {
                  "type": "Spacer",
                  "composeModifier": {
                    "operations": [
                      { "type": "Height", "value": 16 }
                    ]
                  },
                  "properties": {
                    "type": "SpacerProps"
                  }
                },
                {
                  "type": "Button",
                  "properties": {
                    "type": "ButtonProps",
                    "onClickEventName": "on_greet_click",
                    "child": {
                      "type": "Text",
                      "properties": {
                        "type": "TextProps",
                        "text": "Send Greeting"
                      }
                    }
                  }
                }
              ]
            }
          }
        }
      }
    ]
  }
}
""".trimIndent()
