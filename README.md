# Attendence-Marker-App
Android app for remote employee attendance. Captures face and workplace images with live GPS. Uses Firebase for auth, image storage, and attendance data. Includes 10-day history view and admin panel to track employee attendance.

Images:

- This is the Signup/Login page for the app, with Google Signin supported.
![IMG_20250624_161020](https://github.com/user-attachments/assets/0096f991-4120-4bba-a2e4-efb04e832fef)


- This is the landing page, once the user has logged in. User's attendance status of current date will be displayed with three buttons for Marking Attendance, to check attendance history and to logout.
  
![IMG_20250624_161032](https://github.com/user-attachments/assets/36039b46-1c6c-4d27-a6a0-613f9d15edef)

- This is the page where attendance is marked.
- First the Face of the user has to be captured and uploaded.
- The status of IMG upload is shown for convenience using status bar.
- Then the Work location img has to be captured and uploaded.
- After that when the user presses "Mark Attendance" button then the user's current location is fetched in baground through GPS and uploaded to FireBase server.
![IMG_20250624_161102](https://github.com/user-attachments/assets/563a69d6-264c-4460-b17f-5ca6fb68df38)
![IMG_20250624_161121](https://github.com/user-attachments/assets/9715912a-dc14-46bb-b94e-a21c7c114081)


- User can check their attendance history in this page.
  ![IMG_20250624_161149](https://github.com/user-attachments/assets/4f584ef2-b9fc-4537-8132-d9f2161a8aa0)


- This is the Landing page for Admin/Employer once they are logged in
- Admin/Employer can check the location, face img and work location img of any employees.
   ![IMG_20250624_161208](https://github.com/user-attachments/assets/56d3539f-2ccf-4943-ae76-d12f0358928c)
