# 🥘 Mandhi House — Premium Online Ordering Website

Welcome to the official repository for **Mandhi House**, a modern, high-end online food ordering web application designed specifically for a premium dining experience.

🔗 **Live Link:** [https://akazz-333.github.io/Mandhi-Restaurent/](https://akazz-333.github.io/Mandhi-Restaurent/)

---

## ✨ Features

- **🎨 Premium Visual Design**: Uses rich HSL-tailored color schemes, sleek typography (Google Fonts Outfit/Manrope), custom animations, and a cohesive dark/light mode toggle.
- **📱 World-Class Mobile Responsiveness**: Crafted from the ground up for seamless compatibility with mobile and tablet viewports, ensuring a professional touch-scroll experience.
- **🌀 Gooey Navigation Tabs**: Interactive category pills navigation (Show All, Al Fahm, Mandhi, Shakes) using SVG filters to create a beautiful fluid selection animation.
- **🔍 Seamless Mobile Search**: Compact search icon that expands smoothly into a full-width search input on a dedicated line directly below category navigation.
- **🛒 Dynamic Shopping Cart**: Full checkout logic featuring order item quantity modifiers, local item storage, and an elegant side cart drawer popover.
- **🍗 Rich Food Gallery**: Features circular category showcases and detailed product card galleries representing the signature menu of the restaurant.

---

## 🛠️ Technology Stack

- **Frontend Core**: HTML5, Vanilla JavaScript (ES6+), and CSS3 Custom Variables.
- **Animations & Effects**: CSS transitions, keyframes, and custom SVG filters for gooey rendering.
- **Icons**: SVG vectors for zero dependencies and high performance.
- **Backend Server**: Node.js & Express (located in the `/backend` folder).

---

## 📂 Project Structure

```bash
├── .github/          # GitHub Pages build and deploy configurations
├── backend/          # Node.js backend server code
│   └── server.js     # Server entry point
├── frontend/         # Frontend web assets
│   ├── index.html    # Main restaurant landing page
│   ├── Mandhi.css    # Premium CSS design system & responsive layout
│   ├── Mandhi.js     # Frontend menu logic, cart system & interactive effects
│   └── assets/       # Food menu images and graphics
└── index.html        # Redirect page for hosting
```

---

## 🚀 How to Run Locally

### 1. Frontend
Simply open the `frontend/index.html` file in any modern web browser, or serve it using a local development server like Live Server in VS Code.

### 2. Backend
Navigate to the `backend/` directory, install the dependencies, and start the local server:
```bash
cd backend
npm install
npm start
```
