# `javac` CompileNow

This project is an online Java compiler that allows users to write, run, and debug Java code directly in the browser.

The user interface is composed of a code editor powered by the Monaco editor, an input area where the user can provide input for the code, an output area where the results of the code execution will be displayed, and a "Run" button that triggers the code execution.

The code editor uses the Monaco editor, which is a powerful and feature-rich code editor that provides syntax highlighting, code completion, and error checking for Java.

<img width="1125" alt="image" src="https://user-images.githubusercontent.com/429073/212458399-b9d3d731-baaa-451b-bb0e-20bcb0ad297f.png">

The code editor uses the Monaco editor, which is a powerful and feature-rich code editor that provides syntax highlighting, code completion, and error checking for Java.

The input and output areas are standard text areas that allow the user to input data and view the results of the code execution.

When the user clicks the "Run" button, the code is executed and the output, status and execution time of the code is displayed in the output area and the execution time label, respectively. The label of the status also indicates if the code execution is successful or not.

The user interface is designed to be sleek and user-friendly, with all the elements laid out in a clear and intuitive manner. The project also uses CSS to style the component and make it look more attractive.

The project uses React and the react-monaco-editor package, which allows you to use the Monaco editor in a React application. The project also uses the performance API to calculate the execution time of the code.

Overall, this project is a powerful and user-friendly tool for Java developers, providing a convenient and efficient way to write, and run Java code in the browser.


## How to Run the UI
The UI is built using React, so after cloning the repository, run the following command:

`yarn install && yarn run`

This will start the UI on your browser.

## How to Run the Backend
The backend application is a traditional Spring Boot application, but it requires Java 19.

**NOTE:** 

This project took inspiration and idea from this: [OnlineExecutor](https://github.com/TangBean/OnlineExecutor) and did a few things differently. 
