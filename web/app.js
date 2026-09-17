// EduCore Web Platform Client Logic
// Connects to Google Firebase Firestore directly

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.9.0/firebase-app.js";
import { 
  getFirestore, 
  collection, 
  getDocs, 
  doc, 
  setDoc, 
  query, 
  where,
  orderBy,
  limit,
  onSnapshot 
} from "https://www.gstatic.com/firebasejs/10.9.0/firebase-firestore.js";

// Firebase Configuration matching the project
const firebaseConfig = {
  apiKey: "AIzaSyDlu-o2BgI8cwGeJdmvqJ39rZBVQMk_u7c",
  authDomain: "educore-app-a0155.firebaseapp.com",
  projectId: "educore-app-a0155",
  storageBucket: "educore-app-a0155.firebasestorage.app",
  messagingSenderId: "855006195292",
  appId: "1:855006195292:android:47920deda80e8636234099"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// State cache
let studentsCache = [];
let groupsCache = [];
let html5QrCode = null;

// Tab Switching
window.switchTab = function(tabName) {
  const tabs = ['student', 'teacher', 'scanner'];
  tabs.forEach(tab => {
    const sec = document.getElementById(`section-${tab}`);
    const btn = document.getElementById(`tab-${tab}-btn`);
    if (tab === tabName) {
      sec.classList.remove('hidden');
      btn.classList.add('tab-active');
      btn.classList.remove('text-slate-600');
    } else {
      sec.classList.add('hidden');
      btn.classList.remove('tab-active');
      btn.classList.add('text-slate-600');
    }
  });

  // Stop scanner if navigating away
  if (tabName !== 'scanner' && html5QrCode) {
    stopScanner();
  }
};

// Modal handlers
window.openModal = function(modalId) {
  document.getElementById(modalId)?.classList.remove('hidden');
};

window.closeModal = function(modalId) {
  document.getElementById(modalId)?.classList.add('hidden');
};

// ==========================================
// 1. REALTIME FIRESTORE DATA SYNC
// ==========================================
async function initRealtimeData() {
  try {
    // Listen to students
    const studentsCol = collection(db, "students");
    onSnapshot(studentsCol, (snapshot) => {
      studentsCache = [];
      snapshot.forEach(doc => studentsCache.push({ id: doc.id, ...doc.data() }));
      renderTeacherStudents(studentsCache);
      updateDashboardStats();
    });

    // Listen to groups
    const groupsCol = collection(db, "groups");
    onSnapshot(groupsCol, (snapshot) => {
      groupsCache = [];
      snapshot.forEach(doc => groupsCache.push({ id: doc.id, ...doc.data() }));
      document.getElementById('stat-total-groups').innerText = groupsCache.length;
    });

  } catch (error) {
    console.error("Firestore sync error:", error);
    document.getElementById('cloud-status-badge').className = "inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200";
    document.getElementById('cloud-status-badge').innerText = "وضع عدم الاتصال (Offline)";
  }
}

// Update Header Statistics
function updateDashboardStats() {
  document.getElementById('stat-total-students').innerText = studentsCache.length;
  
  if (studentsCache.length > 0) {
    const totalAtt = studentsCache.reduce((acc, s) => acc + (s.attendanceRate || 95), 0);
    const avgAtt = Math.round(totalAtt / studentsCache.length);
    document.getElementById('stat-avg-attendance').innerText = `${avgAtt}%`;
    
    const totalWarns = studentsCache.reduce((acc, s) => acc + (s.warningsCount || 0), 0);
    document.getElementById('stat-total-warnings').innerText = totalWarns;
  }
}

// ==========================================
// 2. TEACHER STUDENTS TABLE
// ==========================================
function renderTeacherStudents(students) {
  const tbody = document.getElementById('teacher-students-tbody');
  if (!tbody) return;

  if (students.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="text-center py-8 text-slate-400">
          لا يوجد طلاب مسجلين بعد. يمكنك إضافة طالب جديد بالضغط على "إضافة طالب".
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = students.map(student => `
    <tr class="hover:bg-slate-50/80 transition-colors">
      <td class="py-3 px-4 font-mono text-brand-600 font-bold">${student.id}</td>
      <td class="py-3 px-4 font-extrabold text-slate-900">${student.fullName || 'بدون اسم'}</td>
      <td class="py-3 px-4">
        <span class="px-2.5 py-1 bg-slate-100 text-slate-700 rounded-lg text-xs font-bold">
          ${student.groupName || 'مجموعة عامة'}
        </span>
      </td>
      <td class="py-3 px-4 text-slate-600 dir-ltr text-right">${student.parentPhone || student.phone || '-'}</td>
      <td class="py-3 px-4">
        <span class="px-2.5 py-1 rounded-full text-xs font-bold ${
          (student.attendanceRate || 95) >= 90 ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'
        }">
          ${student.attendanceRate || 95}%
        </span>
      </td>
      <td class="py-3 px-4 font-bold text-slate-800">${student.averageGrade || 88}%</td>
      <td class="py-3 px-4 text-center">
        <button onclick="viewStudentProfile('${student.id}')" class="px-3 py-1 bg-brand-50 hover:bg-brand-100 text-brand-600 rounded-lg text-xs font-bold transition-all">
          عرض الملف
        </button>
      </td>
    </tr>
  `).join('');
}

window.filterTeacherStudents = function(term) {
  const cleanTerm = term.trim().toLowerCase();
  if (!cleanTerm) {
    renderTeacherStudents(studentsCache);
    return;
  }
  const filtered = studentsCache.filter(s => 
    (s.fullName && s.fullName.toLowerCase().includes(cleanTerm)) ||
    (s.id && s.id.toLowerCase().includes(cleanTerm)) ||
    (s.groupName && s.groupName.toLowerCase().includes(cleanTerm))
  );
  renderTeacherStudents(filtered);
};

// ==========================================
// 3. STUDENT PORTAL SEARCH & PROFILE
// ==========================================
window.handleStudentSearch = async function(event) {
  event.preventDefault();
  const inputVal = document.getElementById('student-code-input').value.trim();
  if (!inputVal) return;

  // Search in memory cache or query Firestore
  let student = studentsCache.find(s => 
    s.id.toLowerCase() === inputVal.toLowerCase() || 
    (s.phone && s.phone === inputVal) ||
    (s.parentPhone && s.parentPhone === inputVal) ||
    (s.fullName && s.fullName.includes(inputVal))
  );

  if (!student) {
    // If not in cache, fallback query
    const docRef = doc(db, "students", inputVal);
    const docSnap = await getDocs(query(collection(db, "students"), where("id", "==", inputVal)));
    if (!docSnap.empty) {
      student = docSnap.docs[0].data();
    }
  }

  if (student) {
    displayStudentProfile(student);
  } else {
    alert("لم يتم العثور على طالب بهذا الكود! تأكد من إدخال الكود الصحيح.");
  }
};

window.viewStudentProfile = function(studentId) {
  const student = studentsCache.find(s => s.id === studentId);
  if (student) {
    window.switchTab('student');
    displayStudentProfile(student);
    window.scrollTo({ top: 300, behavior: 'smooth' });
  }
};

function displayStudentProfile(student) {
  const container = document.getElementById('student-result-container');
  container.classList.remove('hidden');

  document.getElementById('student-name').innerText = student.fullName;
  document.getElementById('student-code-display').innerText = student.id;
  document.getElementById('student-group-badge').innerText = student.groupName || 'المجموعة الأساسية';
  document.getElementById('student-parent-phone').innerText = student.parentPhone || student.phone || 'غير مسجل';
  document.getElementById('student-attendance-rate').innerText = `${student.attendanceRate || 95}%`;
  document.getElementById('student-grade-avg').innerText = `${student.averageGrade || 88}%`;
  document.getElementById('student-warnings-count').innerText = student.warningsCount || 0;

  // Generate QR Code
  const qrDiv = document.getElementById('qrcode-container');
  qrDiv.innerHTML = '';
  new QRCode(qrDiv, {
    text: `EDUCORE_STUDENT:${student.id}`,
    width: 64,
    height: 64,
    colorDark : "#0f3aa8",
    colorLight : "#ffffff",
    correctLevel : QRCode.CorrectLevel.H
  });

  // Mock / Fetched Exam Results
  const gradesTbody = document.getElementById('student-grades-tbody');
  gradesTbody.innerHTML = `
    <tr>
      <td class="py-2.5">اختبار الشهر الأول</td>
      <td class="py-2.5 text-slate-500 font-normal">2026/09/10</td>
      <td class="py-2.5 text-brand-600">48 / 50</td>
      <td class="py-2.5"><span class="px-2 py-0.5 bg-emerald-50 text-emerald-700 rounded text-xs">ممتاز</span></td>
    </tr>
    <tr>
      <td class="py-2.5">تسميع الكلمات والواجب</td>
      <td class="py-2.5 text-slate-500 font-normal">2026/09/14</td>
      <td class="py-2.5 text-brand-600">20 / 20</td>
      <td class="py-2.5"><span class="px-2 py-0.5 bg-emerald-50 text-emerald-700 rounded text-xs">ممتاز</span></td>
    </tr>
  `;

  // Mock / Fetched Attendance list
  const attList = document.getElementById('student-attendance-list');
  attList.innerHTML = `
    <div class="flex justify-between items-center p-3 bg-slate-50 rounded-2xl">
      <div class="flex items-center gap-2">
        <span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
        <span>حصة الأسبوع الحالي</span>
      </div>
      <span class="text-xs text-emerald-700 bg-emerald-100/70 px-2.5 py-1 rounded-full font-bold">حاضر (في الموعد)</span>
    </div>
    <div class="flex justify-between items-center p-3 bg-slate-50 rounded-2xl">
      <div class="flex items-center gap-2">
        <span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
        <span>حصة الأسبوع الماضي</span>
      </div>
      <span class="text-xs text-emerald-700 bg-emerald-100/70 px-2.5 py-1 rounded-full font-bold">حاضر</span>
    </div>
  `;
}

// ==========================================
// 4. ADD STUDENT TO CLOUD
// ==========================================
window.handleAddStudent = async function(event) {
  event.preventDefault();
  const name = document.getElementById('modal-student-name').value.trim();
  const id = document.getElementById('modal-student-id').value.trim();
  const group = document.getElementById('modal-student-group').value.trim();
  const parentPhone = document.getElementById('modal-student-parent-phone').value.trim();

  try {
    await setDoc(doc(db, "students", id), {
      id: id,
      fullName: name,
      groupName: group,
      parentPhone: parentPhone,
      attendanceRate: 100,
      averageGrade: 100,
      warningsCount: 0,
      updatedAt: Date.now()
    }, { merge: true });

    closeModal('add-student-modal');
    alert(`تم تسجيل الطالب ${name} بالسحابة بنجاح!`);
    document.getElementById('modal-student-name').value = '';
    document.getElementById('modal-student-id').value = '';
    document.getElementById('modal-student-parent-phone').value = '';
  } catch (err) {
    console.error("Error saving student:", err);
    alert("حدث خطأ أثناء الحفظ بالسحابة: " + err.message);
  }
};

// ==========================================
// 5. WEB QR CODE SCANNER (CAMERA)
// ==========================================
window.startScanner = function() {
  html5QrCode = new Html5Qrcode("qr-reader");
  const config = { fps: 10, qrbox: { width: 250, height: 250 } };

  html5QrCode.start(
    { facingMode: "environment" },
    config,
    onScanSuccess,
    onScanFailure
  ).then(() => {
    document.getElementById('start-scanner-btn').classList.add('hidden');
    document.getElementById('stop-scanner-btn').classList.remove('hidden');
  }).catch(err => {
    console.error("Camera access error:", err);
    alert("تعذر فتح الكاميرا! يرجى منح إذن الكاميرا للمتصفح.");
  });
};

window.stopScanner = function() {
  if (html5QrCode) {
    html5QrCode.stop().then(() => {
      document.getElementById('start-scanner-btn').classList.remove('hidden');
      document.getElementById('stop-scanner-btn').classList.add('hidden');
      document.getElementById('qr-reader').innerHTML = `<p class="text-slate-400 text-sm">تم إيقاف الكاميرا</p>`;
    });
  }
};

async function onScanSuccess(decodedText) {
  const feedback = document.getElementById('scanner-feedback');
  feedback.classList.remove('hidden');
  
  // Extract ID if formatted
  let studentId = decodedText;
  if (decodedText.startsWith("EDUCORE_STUDENT:")) {
    studentId = decodedText.replace("EDUCORE_STUDENT:", "");
  }

  // Record Attendance in Firestore
  const recordId = `ATT_${studentId}_${Date.now()}`;
  try {
    await setDoc(doc(db, "attendance_records", recordId), {
      id: recordId,
      studentId: studentId,
      date: new Date().toISOString().split('T')[0],
      checkInTime: new Date().toLocaleTimeString('ar-EG'),
      status: "حاضر",
      updatedAt: Date.now()
    });

    feedback.className = "mt-4 p-4 rounded-2xl text-sm font-bold bg-emerald-50 text-emerald-800 border border-emerald-200";
    feedback.innerHTML = `✅ تم تسجيل حضور الطالب بنجاح! (كود: ${studentId})`;

    // Audio confirmation beep
    const audio = new Audio("https://actions.google.com/sounds/v1/alarms/beep_short.ogg");
    audio.play().catch(() => {});

  } catch (err) {
    feedback.className = "mt-4 p-4 rounded-2xl text-sm font-bold bg-rose-50 text-rose-800 border border-rose-200";
    feedback.innerHTML = `❌ فشل تسجيل الحضور: ${err.message}`;
  }
}

function onScanFailure(error) {
  // Silent frame scan failure
}

window.printStudentCard = function() {
  window.print();
};

window.refreshData = function() {
  initRealtimeData();
};

// Kickoff
initRealtimeData();
