# Attendence-Marker-App
Android app for remote employee attendance. Captures face and workplace images with live GPS. Uses Firebase for auth, image storage, and attendance data. Includes 10-day history view and admin panel to track employee attendance.

Images:

- This is the Signup/Login page for the app, with Google Signin supported.
  ![IMG_20250624_161020](https://github.com/user-attachments/assets/29fba56e-0709-491a-9563-dc8124b53d62)



- This is the landing page, once the user has logged in. User's attendance status of current date will be displayed with three buttons for Marking Attendance, to check attendance history and to logout.
   ![IMG_20250624_161032](https://github.com/user-attachments/assets/d22a7221-b1ff-40d0-9436-7cedcbbe88e6)


- This is the page where attendance is marked.
- First the Face of the user has to be captured and uploaded.
- The status of IMG upload is shown for convenience using status bar.
- Then the Work location img has to be captured and uploaded.
- After that when the user presses "Mark Attendance" button then the user's current location is fetched in baground through GPS and uploaded to FireBase server.
   ![IMG_20250624_161102](https://github.com/user-attachments/assets/74fdca89-84c2-48ba-9fdc-e093869ce576)
   ![IMG_20250624_161121](https://github.com/user-attachments/assets/219355bf-5ac0-4c77-8de1-ed665229d7d7)



- User can check their attendance history in this page.
   ![IMG_20250624_161149](https://github.com/user-attachments/assets/b9e180a2-0003-46c9-8748-f4e4ad1be621)


- This is the Landing page for Admin/Employer once they are logged in
- Admin/Employer can check the location, face img and work location img of any employees.
- Admin also has an option of giving a user Admin access through "Add Admin" button in Options Menu.
  ![IMG_20250624_161208](https://github.com/user-attachments/assets/533725eb-1378-44bf-950f-c96e8b1abf02)

