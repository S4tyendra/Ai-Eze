# AiZe

AiZe is an Android application that brings AI-powered text processing to any app on your device. Select text anywhere, and AiZe appears in the context menu to help you rephrase, summarize, translate, or transform your text using Google's Gemini AI.

## Features

**System-Wide Integration**

AiZe integrates directly with Android's text selection menu. When you select text in any application, AiZe appears as an option, allowing you to process that text without switching apps or copying and pasting.

**Text Transformations**

- Rephrase text while preserving meaning
- Fix grammar and spelling mistakes
- Summarize long passages
- Expand brief notes into detailed content

**Tone Adjustments**

- Make text more formal for professional communication
- Convert to casual language for friendly messages
- Add politeness for sensitive conversations

**Formatting and Clarity**

- Convert paragraphs into bullet points
- Simplify complex text for easier understanding

**Translation**

- Translate between Hindi and English

**Smart Reply**

- Generate contextual reply suggestions based on the selected text

## Requirements

- Android 9.0 (Pie) or higher
- A Google Gemini API key (free tier available from Google AI Studio)
- Internet connection for AI processing

## Setup

1. Download and install AiZe on your Android device
2. Open the app and tap on "Setup API Key"
3. Visit [Google AI Studio](https://aistudio.google.com/app/apikey) to create your free API key
4. Paste your API key into the app
5. Your key is stored securely using encrypted storage

## Usage

1. Open any app and select text
2. Tap the "AiZe" option in the text selection menu
3. Choose an action from the available categories
4. Wait for the AI to process your text
5. Copy the result or replace the original text directly

## Architecture

The app follows a clean architecture with the following components:

- **MainActivity**: Entry point with setup instructions and API key management
- **TextHandlerActivity**: Handles the PROCESS_TEXT intent from other apps
- **TextProcessingBottomSheet**: The main UI for action selection and results
- **GeminiClient**: Manages communication with the Gemini AI API
- **ApiKeyManager**: Handles secure storage of API credentials

## Technology Stack

**UI Framework**
- Jetpack Compose with Material Design 3
- Smooth animations and transitions

**Networking**
- Ktor client for HTTP communication
- Kotlinx Serialization for JSON parsing
- Streaming responses for real-time feedback

**Security**
- AndroidX Security Crypto for encrypted SharedPreferences
- API keys never leave the device unencrypted

## Building from Source

Prerequisites:
- Android Studio Ladybug or newer
- JDK 11 or higher

Steps:
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run on a device or emulator running Android 9.0+

## Configuration

The app requires a Gemini API key to function. You can obtain one for free from Google AI Studio. The free tier includes generous usage limits suitable for personal use.

API keys are stored locally on your device using Android's EncryptedSharedPreferences, which provides AES-256 encryption.

## Project Structure

```
app/src/main/java/in/devh/ai_ze/
├── MainActivity.kt              # App entry and setup
├── TextHandlerActivity.kt       # Process text intent handler
├── TextProcessingBottomSheet.kt # Main processing UI
├── GeminiClient.kt              # AI API communication
├── ApiKeyManager.kt             # Secure key storage
├── ApiKeySetupDialog.kt         # Key setup interface
├── PromptTemplates.kt           # AI prompt definitions
├── ActionCategories.kt          # Action organization
└── ui/theme/                    # Material theming
```

## Privacy

AiZe processes text by sending it to Google's Gemini API. No text is stored by the app beyond the current session. Your API key is stored locally on your device in encrypted form and is only used to authenticate requests to the Gemini API.

## License

This project is provided as-is for personal use.

## Contributing

Contributions are welcome. Please open an issue to discuss proposed changes before submitting a pull request.
