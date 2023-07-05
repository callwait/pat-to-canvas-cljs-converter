# PAT to Canvas Converter

PAT to Canvas Converter is a ClojureScript-based library for converting PAT (Pattern) files into HTML Canvas elements. This library aims to simplify the process of displaying patterns in web applications and offers seamless integration with modern web technologies.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
    - [Requirements](#requirements)
    - [Installation](#installation)
- [Usage](#usage)
- [Examples](#examples)
- [Contributing](#contributing)
- [License](#license)

## Features

- Convert PAT files to HTML Canvas elements
- Seamless integration with web applications
- Maintain the integrity of the original pattern data

## Getting Started

These instructions will help you set up the library on your local machine for development and testing purposes.

### Requirements

- Web Browser with support for JavaScript, HTML Canvas elements
- Shadow CLJS

## Usage

To use the library, follow these steps:

1. Create an HTML `<canvas>` element and set its respective `id` or `class` attributes.
```html
<canvas id="myCanvas"></canvas>
```

2. Include the PAT to Canvas Converter library file

3. Use the library in your ClojureScript code, as shown in the example below:

```clojure
(ns my-app.core
  (:require [app.engine :as engine]))

;; Load a PAT file
(engine/load-pat-file "/patterns/HBFLEMET.pat"
    (fn [pattern]
        (let [canvas (js/document.getElementById "myCanvas")]
        (engine/draw-pattern canvas pattern))))

## Examples

For detailed examples of how to use the PAT to Canva Converter library, refer to the `app/views.cljs` file in this repository.

## Contributing

Contributions are welcome! To get started, follow these steps:

1. Fork the repository.
2. Create a new branch: `git checkout -b feature/new-feature`.
3. Make your changes and then commit them: `git commit -am "Add new feature"`.
4. Push the branch to your fork: `git push origin feature/new-feature`.
5. Open a pull request against the upstream repository.

Please ensure compliance with the library's code of conduct and contributing guidelines.

## License

Distributed under the MIT License. See `LICENSE` for more information.
