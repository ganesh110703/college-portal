
import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import "./style.css";

function App() {
  const [students, setStudents] = useState([]);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [course, setCourse] = useState("Computer Science");
  const [message, setMessage] = useState("");

  async function loadStudents() {
    try {
      const response = await fetch("/api/students");
      if (!response.ok) throw new Error("Could not load students");
      setStudents(await response.json());
    } catch (error) {
      setMessage(error.message);
    }
  }

  useEffect(() => {
    loadStudents();
  }, []);

  async function addStudent(event) {
    event.preventDefault();
    setMessage("");

    try {
      const response = await fetch("/api/students", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email, course })
      });

      const result = await response.json();
      if (!response.ok) throw new Error(result.error || "Request failed");

      setName("");
      setEmail("");
      setCourse("Computer Science");
      setMessage("Student added successfully!");
      await loadStudents();
    } catch (error) {
      setMessage(error.message);
    }
  }

  return (
    <main className="container">
      <header>
        <div className="logo">C+</div>
        <div>
          <h1>College Portal</h1>
          <p>Student Management Dashboard</p>
        </div>
      </header>

      <section className="welcome">
        <p>COLLEGE ADMINISTRATION</p>
        <h2>Manage your students.</h2>
        <span>Register students and view their records.</span>
      </section>

      <section className="layout">
        <form className="panel" onSubmit={addStudent}>
          <h2>Add Student</h2>

          <label htmlFor="name">Full Name</label>
          <input
            id="name"
            value={name}
            onChange={e => setName(e.target.value)}
            placeholder="Enter student name"
            required
          />

          <label htmlFor="email">Email Address</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            placeholder="student@example.com"
            required
          />

          <label htmlFor="course">Course</label>
          <select
            id="course"
            value={course}
            onChange={e => setCourse(e.target.value)}
          >
            <option>Computer Science</option>
            <option>Information Technology</option>
            <option>Data Science</option>
            <option>Electronics</option>
          </select>

          <button type="submit">Add Student +</button>
          {message && <p className="message">{message}</p>}
        </form>

        <section className="panel">
          <h2>Student Directory</h2>
          <p>{students.length} registered students</p>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Course</th>
                  <th>Email</th>
                </tr>
              </thead>
              <tbody>
                {students.map(student => (
                  <tr key={student.id}>
                    <td>{student.name}</td>
                    <td>{student.course}</td>
                    <td>{student.email}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <footer>College Portal · React · Java · MySQL · Docker</footer>
    </main>
  );
}

createRoot(document.getElementById("root")).render(<App />);
