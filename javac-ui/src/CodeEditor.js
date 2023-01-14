import './CodeEditor.css';

import React, {useState} from 'react';
import axios from 'axios';
import MonacoEditor from 'react-monaco-editor';

function CodeEditor() {
  const sampleCode = "public class HelloWorld {\n" +
    "  public static void main(String[] args) {\n" +
    "    System.out.println(\"Hello world!\");\n" +
    "  }\n" +
    "}";
  const [code, setCode] = useState(sampleCode);
  const [input, setInput] = useState('');
  const [output, setOutput] = useState('');
  const [status, setStatus] = useState('Not Run');
  const [executionTime, setExecutionTime] = useState(0);

  const handleCodeChange = (newValue) => {
    setCode(newValue);
  };

  const handleInputChange = (event) => {
    setInput(event.target.value);
  };

  const handleRunClick = async () => {
    try {
      const response = await axios.post(
        'http://localhost:8080/api/v1/java/run-code', {code, input});
      console.log(response);
      setOutput(response.data.result);
      setStatus(response.data.status);
      setExecutionTime(response.data.executionTime);
    } catch (error) {
      setOutput(error.message);
    }
  };

  return (
    <div>
      <h1 style={{textAlign: 'center'}}>Online Java Compiler</h1>
      <div className="code-editor">
        <div className="code-editor__section">
          <label>Code</label>
          <div style={{ display: 'flex', justifyContent: 'center' }}>
            <MonacoEditor
              width="100%"
              height="300px"
              language="java"
              theme="vs-dark"
              value={code}
              options={{
                fontSize: "14px",
                scrollBeyondLastLine:false,
                formatOnPaste: true,
                selectOnLineNumbers: true
              }}
              onChange={handleCodeChange}
            />
          </div>
        </div>
        <div className="code-editor__section">
          <label>Input</label>
          <textarea className="code-editor__textarea_small" value={input}
                    onChange={handleInputChange}
                    placeholder="Enter standard input"/>
        </div>
        <div className="code-editor__section">
          <label>Output</label>
          <pre className="code-editor__output">{output}</pre>
        </div>
        <br/>
        <div style={{display: 'flex', justifyContent: 'center'}}>
          <label style={{color: status === 'SUCCEED' ? 'green' : 'red'}}>
            Status: {status}
          </label>
          <label style={{marginLeft: '20px'}}>
            Execution Time: {executionTime}ms
          </label>
        </div>
        <br/>
        <div className="code-editor__section code-editor__section--center">
          <button className="code-editor__button" onClick={handleRunClick}>Run
          </button>
        </div>
      </div>
    </div>
  );
}

export default CodeEditor;
