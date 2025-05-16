# ChatSocket

A simple mod providing a web socket connection for sending and receiving chat messages / commands. Made for lazy people having Minecraft in the background while
playing other games or developing.

## Config

Ingame Config is available via owo-config (also accessible via mod-menu). There you can configure

- port (default: 8080)
- host: (default: 127.0.0.1)
- enabled: (default: true)
- communication type: (default: PLAIN_TEXT)
  - PLAIN_TEXT
  - JSON

## Usage

For testing purposes, I recommend using a browser extension like Weasel Websocket. Once you connected
to the websocket (`ws://127.0.0.1:8080`), you will already receive messages and be able to send some.

For different purposes, there are currently two modes: ``PLAIN_TEXT`` and `JSON`.


### PLAIN_TEXT Mode

In this mode, messages are being sent and received in full plain text. No json whatsoever.
This is intended for IntelliJ websocket plugins or other tools that are not able to parse JSON.

Mind that the communicationType update event is still being sent as a json object.

### JSON Mode

To provide more flexibility, the JSON mode is available. In this mode, messages are being sent and received as JSON objects.
The following JSON objects are being sent:

### Incoming messages
#### Chat Message

````json5
{
  "type": "chat",
  "messageString": "Hello World",
  "gameProfile": {
    "uuid": "8661e1be-bbf5-4cd6-86ba-60cc35f8f7e0",
    "name": "AriVanHouten"
  },
  "timestamp": "2011-12-03T10:15:30Z"
}
````

#### Game Message

````json5
// most of the time these are being sent by the server
{
  "type": "game",
  "messageString": "Hello World"
}
````

#### CommunicationType Change

````json5
// Will be sent regardless of the mode
{
  "type": "communicationType",
  "communicationType": "PLAIN_TEXT" // or "JSON"
}
````

#### Error

````json5
// Will be sent regardless of the mode
{
  type: "error",
  "error": "Error message"
}
````

### Outgoing messages
#### Sending a chat message
````json5
{
  "type": "chat", // Alternatively: 'command' => will only prepend a slash to the message
  "message": "Hello World",
}
````