# SFU Campus Event Explorer

The **SFU Campus Event Explorer** makes planning and discovering on-campus events easier, allowing students to create, RSVP to, and save memories of past events.

### Key Features

- Plan and explore events taking place on SFU Campus
- RSVP to events to help hosts organize
- Save memories from attended events in your profile
- Receive live updates on event status via a Home Screen widget
- Explore other event-goers’ profiles and view public memories
- Use an interactive decision maker to help choose events

##### Webpage: https://delicia0314.wixsite.com/campus-event-explore

##### GitHub Repo: https://github.com/Daksh2060/CMPT362-Group-1
---

## Setup & Build Instructions

1. Clone the repository or download ZIP from webpage (linked above):

   ```bash
   git clone <repo-url>
   cd <repo-folder>
   ```

2. Update `local.properties` with your SDK location and Google Maps API key:
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   MAPS_API_KEY=your_google_maps_api_key
   ```
   
3. Build and run.

---

## Detailed Features

### Event & Planning

- Map-based UI
- Location-based drag-and-drop markers
- Event creation and management
- Event planner system
- RSVP system
- User attendance lists
- Host and attendee system

### Social & Community

- Profile editing
- Follower / following system
- Comment system
- Memories from past events

### Accessibility & Interaction

- Text-to-speech accessibility feature
- Swipe-based decision maker

### Utility & Integration

- User authentication system
- Widgets with live data
- Event weather report

---

## AI Usage

- Used Claude to help design the GridView for a social media-like profile page.

- Used Claude to resolve a bug in the planner search bar, where the planner would appear empty on an empty search.

- Used ChatGPT to assign public weather codes to human-readable weather notices.

- Used ChatGPT to help design a scrollable column within the event Dialogbox.

- Used ChatGPT to highlight selected location within the event explorer.

- Used ChatGPT to add a title overlay to images in the profile page.

