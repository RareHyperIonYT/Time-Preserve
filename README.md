# Time Preserve

A lightweight Minecraft plugin that **pauses the daylight cycle** when your server is empty.

Keeps your day counter accurate, and prevents time from flying by while nobody is around.

## Features

- Automatically pauses `doDaylightCycle` whenever the server is inactive.
- Automatically pauses `doWeatherCycle` whenever the server is inactive.
- Restores time progression whenever a player rejoins the server.

## Configuration

<details>
  <summary>config.yml</summary>

  ```yml
  # How long (in minutes) the server has to be empty before pausing.
  idleTimeout: 5
    
  # If you dislike weather, you should disable this otherwise it will enable it again on you.
  effectWeather: true
  ```

</details>

## Support & License

⭐ If you find this project useful, consider giving it a star on GitHub!

📜 This project is under the [MIT License](LICENSE).