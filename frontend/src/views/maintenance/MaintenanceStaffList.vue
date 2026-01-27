<script setup>
import { ref, onMounted, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import maintenanceApi from '@/api/modules/maintenance'
import Swal from 'sweetalert2'
import { useRouter } from 'vue-router'
import { usePagination } from '@/composables/maintenance/usePagination'
import { InfoFilled, Right, Delete, Check } from '@element-plus/icons-vue'

const router = useRouter()
const staffList = ref([])
const searchText = ref('')
const loading = ref(true)
const pageVisible = ref(false)
const sortConfig = ref({ prop: 'staffId', order: 'ascending' })

// ====== el-table 跑版修正：強制重新計算欄寬 ======
const tableRef = ref(null)

const doLayoutSafe = async () => {
  await nextTick()
  tableRef.value?.doLayout?.()
}

const onResize = () => doLayoutSafe()
window.addEventListener('resize', onResize)
onBeforeUnmount(() => window.removeEventListener('resize', onResize))

// ====== 工單快取（用一次 API 建立 staff 狀態）======
const allTickets = ref([])
const ticketLoading = ref(false)

// ====== 狀態篩選（全部 / 維護中 / 維修中 / 閒置中）======
const statusFilter = ref('ALL') // 'ALL' | 'MAINTAINING' | 'REPAIRING' | 'ASSIGNED' | 'IDLE'

// ====== 轉移工單 Dialog 狀態 ======
const showTransferDialog = ref(false)
const transferForm = ref({
  deleteStaffId: null,
  deleteStaffName: '',
  targetStaffId: null,
})
const transferLoading = ref(false)

// ★ 計算可選接手人員：排除要刪除的人 + 必須啟用中
const availableTargetStaff = computed(() => {
  return staffList.value.filter(
    (s) => s.staffId !== transferForm.value.deleteStaffId && s.isActive === true,
  )
})

/** 判斷是否保養任務（你原本就有概念，我保留並集中） */
const isMaintenanceTask = (issueType) => {
  if (!issueType) return false
  const keywords = ['保養', '例行', '檢查']
  return keywords.some((k) => String(issueType).includes(k))
}

/** 判斷是否已完成（你原本就有概念，我保留並集中） */
const isCompletedStatus = (status) => {
  return ['RESOLVED', 'CLOSED', 'CANCELLED'].includes(status)
}

/** 取回 staff + tickets（一次建狀態） */
const fetchStaff = async () => {
  try {
    loading.value = true
    const res = await maintenanceApi.getAllStaff()
    staffList.value = res.data || []
  } catch {
    // 錯誤已由 http.js 攔截器處理
  } finally {
    loading.value = false
  }
}

const fetchTickets = async () => {
  try {
    ticketLoading.value = true
    const res = await maintenanceApi.getAllTickets()
    allTickets.value = res.data || []
  } catch {
    allTickets.value = []
  } finally {
    ticketLoading.value = false
  }
}

/** 建立 staffId -> 工單統計（未完成：維修 / 保養；完成：維修 / 保養） */
const staffTicketStatMap = computed(() => {
  const map = new Map()
  for (const t of allTickets.value) {
    const staffId = t.assignedStaffId
    if (!staffId) continue

    if (!map.has(staffId)) {
      map.set(staffId, {
        repairCurrent: 0,
        maintainCurrent: 0,
        repairAssigned: 0,
        maintainAssigned: 0,
        repairDone: 0,
        maintainDone: 0,
      })
    }
    const stat = map.get(staffId)
    const maintenance = isMaintenanceTask(t.issueType)
    const status = t.issueStatus
    const done = isCompletedStatus(status)
    const assigned = status === 'ASSIGNED'
    const inProgress = status === 'UNDER_MAINTENANCE'

    if (maintenance) {
      if (inProgress) stat.maintainCurrent++
      else if (assigned) stat.maintainAssigned++
      else if (done) stat.maintainDone++
    } else {
      if (inProgress) stat.repairCurrent++
      else if (assigned) stat.repairAssigned++
      else if (done) stat.repairDone++
    }
  }
  return map
})

/** 人員目前狀態（維護中 > 維修中 > 閒置中） */
const getStaffWorkStatus = (staffId) => {
  const stat = staffTicketStatMap.value.get(staffId) || {
    repairCurrent: 0,
    maintainCurrent: 0,
    repairAssigned: 0,
    maintainAssigned: 0,
    repairDone: 0,
    maintainDone: 0,
  }

  if (stat.maintainCurrent > 0) {
    return { key: 'MAINTAINING', text: '維護中', tagType: 'info', icon: 'fas fa-clipboard-check' }
  }
  if (stat.repairCurrent > 0) {
    return { key: 'REPAIRING', text: '維修中', tagType: 'warning', icon: 'fas fa-wrench' }
  }

  const assignedTotal = stat.maintainAssigned + stat.repairAssigned
  if (assignedTotal > 0) {
    return { key: 'ASSIGNED', text: '已指派', tagType: 'primary', icon: 'fas fa-user-check' }
  }

  return { key: 'IDLE', text: '閒置中', tagType: 'success', icon: 'fas fa-mug-hot' }
}

/** ====== 刪除人員（含防呆轉移邏輯）====== */
const handleDelete = async (row) => {
  const result = await Swal.fire({
    title: '確定要停用此人員嗎？',
    html: `
      <div style="text-align: center;">
        <div style="width: 80px; height: 80px; margin: 0 auto 16px; background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center;">
          <i class="fas fa-user-slash" style="font-size: 36px; color: white;"></i>
        </div>
        <p style="font-size: 16px; margin-bottom: 8px;">即將停用 <b style="color: #f56c6c;">${row.staffName}</b></p>
        <p style="color: #909399; font-size: 13px;">刪除的資料可在歷史紀錄中查看並恢復</p>
      </div>
    `,
    icon: null,
    showCancelButton: true,
    confirmButtonColor: '#f56c6c',
    cancelButtonColor: '#909399',
    confirmButtonText: '<i class="fas fa-user-slash mr-1"></i> 確認停用',
    cancelButtonText: '取消',
    showClass: { popup: 'animate__animated animate__fadeInDown animate__faster' },
    hideClass: { popup: 'animate__animated animate__fadeOutUp animate__faster' },
    customClass: { popup: 'custom-swal-popup' },
  })

  if (result.isConfirmed) {
    try {
      await maintenanceApi.deleteStaff(row.staffId)
      await fetchStaff()
      await Swal.fire({
        icon: 'success',
        title: '停用成功',
        html: `<span><b>${row.staffName}</b> 已移至歷史紀錄</span>`,
        timer: 1000,
        timerProgressBar: true,
        showConfirmButton: false,
        showClass: { popup: 'animate__animated animate__bounceIn' },
      })
    } catch (error) {
      const errorMsg = error?.response?.data?.message || error?.message || ''
      if (
        errorMsg.includes('未完成') ||
        errorMsg.includes('工單') ||
        error?.response?.status === 400
      ) {
        transferForm.value = {
          deleteStaffId: row.staffId,
          deleteStaffName: row.staffName,
          targetStaffId: null,
        }
        showTransferDialog.value = true
      }
    }
  }
}

/** ====== 重要：四卡點擊 -> 歷史清單（不依賴不存在的 API，改用 allTickets 篩）====== */
const showHistoryModal = async (staffId, cardType, staffName) => {
  // 保證 tickets 已載入（避免第一次點擊沒資料）
  if (!allTickets.value.length && !ticketLoading.value) {
    await fetchTickets()
  }

  // 先定義查詢條件
  let title = ''
  let iconClass = ''
  let headerGrad = ''
  const matchFn = (t) => t.assignedStaffId === staffId

  const getTargetLabel = (t) => {
    // 你現在列表顯示是：椅子#seatsId 或 機台#spotId
    // 這裡沿用：若 seatsId 有值，顯示 椅子#xxx，否則 機台#yyy
    if (t.seatsId) return `椅子 #${t.seatsId}`
    if (t.spotId) return `機台 #${t.spotId}`
    return '未指定'
  }

  // 依卡片類型篩 tickets
  let filtered = []
  switch (cardType) {
    case 'repair-pending':
      title = '待修任務'
      iconClass = 'fas fa-wrench'
      headerGrad = 'linear-gradient(135deg,#f7b55f 0%,#f3d19e 100%)'
      filtered = allTickets.value.filter(
        (t) => matchFn(t) && !isMaintenanceTask(t.issueType) && !isCompletedStatus(t.issueStatus),
      )
      break
    case 'maintenance-pending':
      title = '待保養任務'
      iconClass = 'fas fa-clipboard-list'
      headerGrad = 'linear-gradient(135deg,#6aa8ff 0%,#a0cfff 100%)'
      filtered = allTickets.value.filter(
        (t) => matchFn(t) && isMaintenanceTask(t.issueType) && !isCompletedStatus(t.issueStatus),
      )
      break
    case 'repair-completed':
      title = '維修完成'
      iconClass = 'fas fa-check-circle'
      headerGrad = 'linear-gradient(135deg,#74d39a 0%,#b3e19d 100%)'
      filtered = allTickets.value.filter(
        (t) => matchFn(t) && !isMaintenanceTask(t.issueType) && isCompletedStatus(t.issueStatus),
      )
      break
    case 'maintenance-completed':
      title = '保養完成'
      iconClass = 'fas fa-flag-checkered'
      headerGrad = 'linear-gradient(135deg,#9aa0a6 0%,#c0c4cc 100%)'
      filtered = allTickets.value.filter(
        (t) => matchFn(t) && isMaintenanceTask(t.issueType) && isCompletedStatus(t.issueStatus),
      )
      break
    default:
      title = '工單紀錄'
      iconClass = 'fas fa-ticket-alt'
      headerGrad = 'linear-gradient(135deg,#79bbff 0%,#c6e2ff 100%)'
      filtered = allTickets.value.filter((t) => matchFn(t))
  }

  // 排序：最新在上（用 reportedAt / startAt / resolvedAt）
  const getTime = (t) => new Date(t.reportedAt || t.startAt || t.resolvedAt || 0).getTime()
  filtered.sort((a, b) => getTime(b) - getTime(a))

  const displayTickets = filtered.slice(0, 12)

  const buildRow = (t) => {
    const date = t.reportedAt ? new Date(t.reportedAt).toLocaleString('zh-TW') : '-'
    const target = getTargetLabel(t)
    const desc = t.issueDesc || '無描述'
    const result = t.resolveNote || '-'
    const status = t.issueStatus || '-'
    return `
      <tr style="border-bottom: 1px solid #ebeef5;">
        <td style="padding:10px 8px;"><span style="display:inline-block;background:#f4f4f5;border:1px solid #e9e9eb;border-radius:8px;padding:2px 8px;font-size:12px;">#${t.ticketId}</span></td>
        <td style="padding:10px 8px;font-size:12px;color:#606266;">${date}</td>
        <td style="padding:10px 8px;font-size:12px;color:#303133;">${target}</td>
        <td style="padding:10px 8px;font-size:12px;color:#606266;" title="${desc}">${desc.length > 24 ? desc.slice(0, 24) + '…' : desc}</td>
        <td style="padding:10px 8px;font-size:12px;color:#606266;" title="${result}">${result.length > 18 ? result.slice(0, 18) + '…' : result}</td>
        <td style="padding:10px 8px;font-size:12px;color:#909399;">${status}</td>
      </tr>
    `
  }

  const tableHtml =
    displayTickets.length === 0
      ? `
        <div style="text-align:center;padding:34px 10px;color:#909399;">
          <div style="width:72px;height:72px;margin:0 auto 14px;border-radius:18px;background:#f5f7fa;display:flex;align-items:center;justify-content:center;">
            <i class="fas fa-inbox" style="font-size:34px;opacity:.5;"></i>
          </div>
          <div style="font-size:14px;">暫無 ${title} 記錄</div>
        </div>
      `
      : `
        <div style="max-height:420px;overflow:auto;border:1px solid #ebeef5;border-radius:12px;">
          <table style="width:100%;border-collapse:collapse;">
            <thead>
              <tr style="background:#f8f9fa;border-bottom:1px solid #ebeef5;">
                <th style="text-align:left;padding:10px 8px;font-size:12px;color:#303133;width:10%;">工單</th>
                <th style="text-align:left;padding:10px 8px;font-size:12px;color:#303133;width:18%;">時間</th>
                <th style="text-align:left;padding:10px 8px;font-size:12px;color:#303133;width:14%;">目標</th>
                <th style="text-align:left;padding:10px 8px;font-size:12px;color:#303133;width:28%;">描述</th>
                <th style="text-align:left;padding:10px 8px;font-size:12px;color:#303133;width:20%;">結果</th>
                <th style="text-align:left;padding:10px 8px;font-size:12px;color:#303133;width:10%;">狀態</th>
              </tr>
            </thead>
            <tbody>
              ${displayTickets.map(buildRow).join('')}
            </tbody>
          </table>
        </div>
        ${
          filtered.length > 12
            ? `<div style="text-align:center;margin-top:10px;font-size:12px;color:#909399;">顯示最近 12 筆，共 ${filtered.length} 筆</div>`
            : ''
        }
      `

  await Swal.fire({
    title: `
      <div style="display:flex;align-items:center;justify-content:center;gap:10px;">
        <div style="width:40px;height:40px;border-radius:12px;background:${headerGrad};display:flex;align-items:center;justify-content:center;color:white;">
          <i class="${iconClass}"></i>
        </div>
        <div style="font-size:16px;font-weight:700;color:#303133;">${staffName} · ${title}</div>
      </div>
    `,
    html: tableHtml,
    width: '92%',
    maxWidth: '1050px',
    confirmButtonText: '關閉',
    confirmButtonColor: '#606266',
    showClass: { popup: 'animate__animated animate__fadeInUp animate__faster' },
    hideClass: { popup: 'animate__animated animate__fadeOutDown animate__faster' },
    customClass: { popup: 'custom-swal-popup' },
  })
}

/** ★ 查看詳情（UI 收斂 + 四卡可點擊） */
const viewDetail = async (row) => {
  // 確保 tickets 有資料（否則卡片數字會永遠 0）
  if (!allTickets.value.length && !ticketLoading.value) {
    await fetchTickets()
  }

  const stat = staffTicketStatMap.value.get(row.staffId) || {
    repairCurrent: 0,
    maintainCurrent: 0,
    repairDone: 0,
    maintainDone: 0,
  }

  const status = getStaffWorkStatus(row.staffId)

  Swal.fire({
    title: `
      <div style="display:flex;align-items:center;gap:12px;justify-content:flex-start;">
        <div style="width:52px;height:52px;border-radius:16px;background:linear-gradient(135deg,#4f9cff 0%,#8cc5ff 100%);display:flex;align-items:center;justify-content:center;color:white;font-size:20px;font-weight:800;box-shadow:0 8px 22px rgba(79,156,255,.25);">
          ${row.staffName?.charAt(0) || '?'}
        </div>
        <div style="display:flex;flex-direction:column;align-items:flex-start;gap:4px;">
          <div style="font-size:18px;font-weight:800;color:#303133;line-height:1;">${row.staffName}</div>
          <div style="font-size:12px;color:#909399;display:flex;align-items:center;gap:8px;">
            <span style="display:inline-flex;align-items:center;gap:6px;background:#f5f7fa;border:1px solid #ebeef5;border-radius:999px;padding:2px 10px;">
              <i class="${status.icon}" style="opacity:.85;"></i> ${status.text}
            </span>
            <span>·</span>
            <span>建立：${row.createdAt ? new Date(row.createdAt).toLocaleDateString('zh-TW') : '-'}</span>
          </div>
        </div>
      </div>
    `,
    html: `
      <div style="text-align:left;margin-top:14px;">
        <!-- 四張卡：統一色系（不那麼刺眼） -->
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:14px;">
          <div data-card-type="repair-pending" style="padding:14px;border-radius:14px;background:linear-gradient(135deg,#f6b26b 0%,#f9e0c7 100%);color:#1f2d3d;cursor:pointer;border:1px solid rgba(255,255,255,.7);">
            <div style="display:flex;align-items:center;justify-content:space-between;">
              <div style="font-size:12px;opacity:.85;"><i class="fas fa-wrench mr-1"></i>待修任務</div>
              <div style="width:32px;height:32px;border-radius:12px;background:rgba(255,255,255,.55);display:flex;align-items:center;justify-content:center;">
                <i class="fas fa-arrow-right" style="opacity:.7;"></i>
              </div>
            </div>
            <div style="font-size:26px;font-weight:900;margin-top:6px;">${stat.repairCurrent}</div>
          </div>

          <div data-card-type="maintenance-pending" style="padding:14px;border-radius:14px;background:linear-gradient(135deg,#6aa8ff 0%,#d6e9ff 100%);color:#1f2d3d;cursor:pointer;border:1px solid rgba(255,255,255,.7);">
            <div style="display:flex;align-items:center;justify-content:space-between;">
              <div style="font-size:12px;opacity:.85;"><i class="fas fa-clipboard-list mr-1"></i>待保養</div>
              <div style="width:32px;height:32px;border-radius:12px;background:rgba(255,255,255,.55);display:flex;align-items:center;justify-content:center;">
                <i class="fas fa-arrow-right" style="opacity:.7;"></i>
              </div>
            </div>
            <div style="font-size:26px;font-weight:900;margin-top:6px;">${stat.maintainCurrent}</div>
          </div>

          <div data-card-type="repair-completed" style="padding:14px;border-radius:14px;background:linear-gradient(135deg,#79d7a7 0%,#e4f7ea 100%);color:#1f2d3d;cursor:pointer;border:1px solid rgba(255,255,255,.7);">
            <div style="display:flex;align-items:center;justify-content:space-between;">
              <div style="font-size:12px;opacity:.85;"><i class="fas fa-check-circle mr-1"></i>維修完成</div>
              <div style="width:32px;height:32px;border-radius:12px;background:rgba(255,255,255,.55);display:flex;align-items:center;justify-content:center;">
                <i class="fas fa-arrow-right" style="opacity:.7;"></i>
              </div>
            </div>
            <div style="font-size:26px;font-weight:900;margin-top:6px;">${stat.repairDone}</div>
          </div>

          <div data-card-type="maintenance-completed" style="padding:14px;border-radius:14px;background:linear-gradient(135deg,#aeb4bb 0%,#eef0f2 100%);color:#1f2d3d;cursor:pointer;border:1px solid rgba(255,255,255,.7);">
            <div style="display:flex;align-items:center;justify-content:space-between;">
              <div style="font-size:12px;opacity:.85;"><i class="fas fa-flag-checkered mr-1"></i>保養完成</div>
              <div style="width:32px;height:32px;border-radius:12px;background:rgba(255,255,255,.55);display:flex;align-items:center;justify-content:center;">
                <i class="fas fa-arrow-right" style="opacity:.7;"></i>
              </div>
            </div>
            <div style="font-size:26px;font-weight:900;margin-top:6px;">${stat.maintainDone}</div>
          </div>
        </div>

        <!-- 資料卡：更一致的灰白底 + 左側色條 -->
        <div style="display:grid;gap:12px;">
          <div style="padding:12px 14px;border-radius:14px;background:#f5f7fa;border:1px solid #ebeef5;">
            <div style="font-size:12px;color:#909399;">所屬公司</div>
            <div style="font-size:14px;font-weight:700;color:#303133;margin-top:4px;">
              <i class="fas fa-building mr-2" style="color:#409eff;"></i>${row.staffCompany || '未填寫'}
            </div>
          </div>

          <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
            <div style="padding:12px 14px;border-radius:14px;background:#ffffff;border:1px solid #ebeef5;">
              <div style="font-size:12px;color:#909399;">聯絡電話</div>
              <div style="font-size:14px;font-weight:700;color:#303133;margin-top:4px;">
                <i class="fas fa-phone mr-2" style="color:#67c23a;"></i>${row.staffPhone || '-'}
              </div>
            </div>
            <div style="padding:12px 14px;border-radius:14px;background:#ffffff;border:1px solid #ebeef5;">
              <div style="font-size:12px;color:#909399;">電子郵件</div>
              <div style="font-size:14px;font-weight:700;color:#303133;margin-top:4px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
                <i class="fas fa-envelope mr-2" style="color:#e6a23c;"></i>${row.staffEmail || '-'}
              </div>
            </div>
          </div>

          <div style="padding:12px 14px;border-radius:14px;background:#fffaf0;border:1px solid #f3e1c8;">
            <div style="font-size:12px;color:#909399;">備註說明</div>
            <div style="font-size:13px;color:#606266;margin-top:4px;line-height:1.5;">
              ${row.staffNote || '無備註'}
            </div>
          </div>
        </div>
      </div>
    `,
    showCancelButton: true,
    confirmButtonText: '<i class="fas fa-edit mr-1"></i> 編輯資料',
    cancelButtonText: '關閉',
    confirmButtonColor: '#409eff',
    cancelButtonColor: '#909399',
    width: 560,
    showClass: { popup: 'animate__animated animate__fadeInUp animate__faster' },
    hideClass: { popup: 'animate__animated animate__fadeOutDown animate__faster' },
    // ✅ 這裡是關鍵：把四張卡的 click 綁定起來
    didRender: (popup) => {
      const cards = popup.querySelectorAll('[data-card-type]')
      cards.forEach((card) => {
        card.addEventListener('click', () => {
          const cardType = card.getAttribute('data-card-type')
          showHistoryModal(row.staffId, cardType, row.staffName)
        })
        // 小互動：hover 提升（不用 inline onmouseover）
        card.addEventListener('mouseenter', () => {
          card.style.transform = 'translateY(-2px)'
          card.style.boxShadow = '0 10px 26px rgba(0,0,0,.10)'
          card.style.transition = 'all .18s ease'
        })
        card.addEventListener('mouseleave', () => {
          card.style.transform = 'translateY(0)'
          card.style.boxShadow = 'none'
        })
      })
    },
  }).then((result) => {
    if (result.isConfirmed) {
      router.push(`/admin/staff-form/${row.staffId}`)
    }
  })
}

const handleAddNew = () => {
  Swal.fire({
    title: '新增維護人員',
    text: '即將前往新增人員表單',
    icon: 'info',
    timer: 600,
    timerProgressBar: true,
    showConfirmButton: false,
    showClass: { popup: 'animate__animated animate__fadeInRight animate__faster' },
  }).then(() => {
    router.push('/admin/staff-form')
  })
}

// ====== 執行轉移並刪除 ======
const handleTransferAndDelete = async () => {
  if (!transferForm.value.targetStaffId) {
    await Swal.fire({
      icon: 'warning',
      title: '請選擇接手人員',
      text: '必須指定一位接手人員來接收未完成的工單',
      confirmButtonColor: '#409eff',
    })
    return
  }

  transferLoading.value = true
  try {
    await maintenanceApi.transferAndDelete(
      transferForm.value.targetStaffId,
      transferForm.value.deleteStaffId,
    )

    showTransferDialog.value = false
    await fetchStaff()

    const targetStaff = staffList.value.find((s) => s.staffId === transferForm.value.targetStaffId)

    await Swal.fire({
      icon: 'success',
      title: '轉移成功！',
      html: `
        <div style="text-align: center;">
          <p>工單已轉移給 <b style="color: #67c23a;">${targetStaff?.staffName || '接手人員'}</b></p>
          <p style="color: #909399; font-size: 13px;"><b>${transferForm.value.deleteStaffName}</b> 已停用</p>
        </div>
      `,
      timer: 2000,
      timerProgressBar: true,
      showConfirmButton: false,
      showClass: { popup: 'animate__animated animate__bounceIn' },
    })
  } catch {
    // 錯誤已由 http.js 攔截器處理
  } finally {
    transferLoading.value = false
  }
}

// 關閉 Dialog
const closeTransferDialog = () => {
  showTransferDialog.value = false
  transferForm.value = {
    deleteStaffId: null,
    deleteStaffName: '',
    targetStaffId: null,
  }
}

/** 先過濾 active，再過濾狀態，再做搜尋 */
const filteredList = computed(() => {
  const key = searchText.value.trim().toLowerCase()

  const activeStaff = staffList.value.filter((s) => s.isActive === true)

  const statusFiltered =
    statusFilter.value === 'ALL'
      ? activeStaff
      : activeStaff.filter((s) => getStaffWorkStatus(s.staffId).key === statusFilter.value)

  if (!key) return statusFiltered
  return statusFiltered.filter(
    (s) =>
      (s.staffName || '').toLowerCase().includes(key) ||
      (s.staffCompany || '').toLowerCase().includes(key) ||
      (s.staffPhone || '').includes(key),
  )
})

// 使用 usePagination composable
const {
  currentPage,
  pageSize,
  paginatedList,
  total: paginationTotal,
  showPagination,
  resetPagination,
} = usePagination(filteredList, { defaultPageSize: 10 })

watch([searchText, statusFilter], () => {
  resetPagination()
  doLayoutSafe()
})

watch([currentPage, pageSize], () => {
  doLayoutSafe()
})

const formatDate = (row, column, cellValue) => {
  if (!cellValue) return '-'
  return new Date(cellValue).toLocaleDateString('zh-TW')
}

// ====== 統計卡片數字（以 filteredList / staffList + 狀態 map 計）=====
const activeCount = computed(() => staffList.value.filter((s) => s.isActive).length)
const maintainingCount = computed(
  () =>
    staffList.value.filter((s) => s.isActive && getStaffWorkStatus(s.staffId).key === 'MAINTAINING')
      .length,
)

// 維修中
const repairingCount = computed(
  () =>
    staffList.value.filter((s) => s.isActive && getStaffWorkStatus(s.staffId).key === 'REPAIRING')
      .length,
)

// 閒置中
const idleCount = computed(
  () =>
    staffList.value.filter((s) => s.isActive && getStaffWorkStatus(s.staffId).key === 'IDLE')
      .length,
)

// 已指派（不論是否在職）
const assignedCount = computed(
  () =>
    staffList.value.filter((s) => s.isActive && getStaffWorkStatus(s.staffId).key === 'ASSIGNED')
      .length,
)

onMounted(async () => {
  await fetchStaff()
  await fetchTickets() // ✅ 重要：先載 tickets，名單才能即時有狀態
  setTimeout(() => {
    pageVisible.value = true
    // ✅ 等 transition / DOM 完整後再 layout
    setTimeout(() => doLayoutSafe(), 150)
  }, 100)
})
</script>

<template>
  <div class="staff-list-container">
    <!-- 頁面標題區 -->
    <section class="content-header">
      <div class="container-fluid">
        <transition name="slide-down" appear>
          <div class="page-title-box">
            <div class="title-icon">
              <i class="fas fa-users-cog"></i>
            </div>
            <div class="title-content">
              <h1>維護人員管理</h1>
              <p class="subtitle">管理系統內的維護人員資料</p>
            </div>
            <div class="title-actions">
              <el-button-group>
                <el-button type="success" @click="handleAddNew" class="action-btn add-btn">
                  <i class="fas fa-user-plus mr-2"></i> 新增人員
                </el-button>
                <el-button
                  type="info"
                  plain
                  @click="router.push('/admin/staff-history')"
                  class="action-btn"
                >
                  <i class="fas fa-history mr-2"></i> 歷史紀錄
                </el-button>
              </el-button-group>
            </div>
          </div>
        </transition>
      </div>
    </section>

    <!-- 主內容區 -->
    <section class="content">
      <div class="container-fluid">
        <transition name="zoom-fade" appear>
          <div v-show="pageVisible">
            <!-- 統計卡片列：新增 3 張狀態卡 -->
            <el-row :gutter="20" class="mb-4">
              <el-col :xs="12" :sm="8" :md="6" :lg="4">
                <div class="stat-card active-card" @click="statusFilter = 'ALL'">
                  <div class="stat-icon pulse-animation">
                    <i class="fas fa-user-check"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ activeCount }}</h3>
                    <span>在職人員</span>
                  </div>
                  <div class="stat-bg-icon"><i class="fas fa-users"></i></div>
                </div>
              </el-col>

              <el-col :xs="12" :sm="8" :md="6" :lg="4">
                <div class="stat-card filter-card" @click="statusFilter = 'MAINTAINING'">
                  <div
                    class="stat-icon"
                    style="background: linear-gradient(135deg, #409eff 0%, #79bbff 100%)"
                  >
                    <i class="fas fa-clipboard-check"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ maintainingCount }}</h3>
                    <span>維護中</span>
                  </div>
                </div>
              </el-col>

              <el-col :xs="12" :sm="8" :md="6" :lg="4">
                <div class="stat-card filter-card" @click="statusFilter = 'REPAIRING'">
                  <div
                    class="stat-icon"
                    style="background: linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%)"
                  >
                    <i class="fas fa-wrench"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ repairingCount }}</h3>
                    <span>維修中</span>
                  </div>
                </div>
              </el-col>

              <el-col :xs="12" :sm="8" :md="6" :lg="4">
                <div class="stat-card filter-card" @click="statusFilter = 'ASSIGNED'">
                  <div
                    class="stat-icon"
                    style="background: linear-gradient(135deg, #409eff 0%, #a0cfff 100%)"
                  >
                    <i class="fas fa-user-check"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ assignedCount }}</h3>
                    <span>已指派</span>
                  </div>
                </div>
              </el-col>

              <el-col :xs="12" :sm="8" :md="6" :lg="4">
                <div class="stat-card filter-card" @click="statusFilter = 'IDLE'">
                  <div
                    class="stat-icon"
                    style="background: linear-gradient(135deg, #67c23a 0%, #95d475 100%)"
                  >
                    <i class="fas fa-mug-hot"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ idleCount }}</h3>
                    <span>閒置中</span>
                  </div>
                </div>
              </el-col>

              <el-col :xs="12" :sm="8" :md="6" :lg="4">
                <div class="stat-card filter-card">
                  <div class="stat-icon">
                    <i class="fas fa-search"></i>
                  </div>
                  <div class="stat-info">
                    <h3>{{ filteredList.length }}</h3>
                    <span>搜尋結果</span>
                  </div>
                </div>
              </el-col>
            </el-row>

            <!-- 資料表格卡片 -->
            <el-card shadow="hover" class="table-card">
              <template #header>
                <div class="card-header-content">
                  <div class="header-left">
                    <span class="header-icon">
                      <i class="fas fa-list-ul"></i>
                    </span>
                    <span class="header-text">人員名單</span>

                    <!-- 狀態 filter pills -->
                    <div class="ml-2" style="display: flex; gap: 6px; flex-wrap: wrap">
                      <el-tag
                        :effect="statusFilter === 'ALL' ? 'dark' : 'plain'"
                        type="info"
                        round
                        @click="statusFilter = 'ALL'"
                        style="cursor: pointer"
                      >
                        全部
                      </el-tag>
                      <el-tag
                        :effect="statusFilter === 'MAINTAINING' ? 'dark' : 'plain'"
                        type="info"
                        round
                        @click="statusFilter = 'MAINTAINING'"
                        style="cursor: pointer"
                      >
                        維護中
                      </el-tag>
                      <el-tag
                        :effect="statusFilter === 'REPAIRING' ? 'dark' : 'plain'"
                        type="warning"
                        round
                        @click="statusFilter = 'REPAIRING'"
                        style="cursor: pointer"
                      >
                        維修中
                      </el-tag>

                      <el-tag
                        :effect="statusFilter === 'ASSIGNED' ? 'dark' : 'plain'"
                        type="primary"
                        round
                        @click="statusFilter = 'ASSIGNED'"
                        style="cursor: pointer"
                      >
                        已指派
                      </el-tag>

                      <el-tag
                        :effect="statusFilter === 'IDLE' ? 'dark' : 'plain'"
                        type="success"
                        round
                        @click="statusFilter = 'IDLE'"
                        style="cursor: pointer"
                      >
                        閒置中
                      </el-tag>
                    </div>

                    <el-tag type="success" effect="light" size="small" class="ml-2" round>
                      <i class="fas fa-circle" style="font-size: 6px; margin-right: 4px"></i>
                      {{ filteredList.length }} 位
                    </el-tag>
                  </div>

                  <div class="header-right">
                    <el-input
                      v-model="searchText"
                      placeholder="搜尋姓名、公司、電話..."
                      prefix-icon="Search"
                      clearable
                      class="search-input"
                    >
                      <template #append>
                        <el-button
                          icon="Refresh"
                          @click="
                            () => {
                              fetchStaff()
                              fetchTickets()
                            }
                          "
                        />
                      </template>
                    </el-input>
                  </div>
                </div>
              </template>

              <!-- 骨架屏載入 -->
              <div v-if="loading" class="loading-skeleton">
                <el-skeleton :rows="6" animated />
              </div>

              <!-- 資料表格 -->
              <el-table
                ref="tableRef"
                v-else
                :data="paginatedList"
                stripe
                highlight-current-row
                class="custom-table"
                style="width: 100%"
                table-layout="fixed"
                :header-cell-style="{ boxSizing: 'border-box' }"
                :cell-style="{ boxSizing: 'border-box' }"
                @row-dblclick="viewDetail"
              >
                <el-table-column prop="staffId" label="ID" width="70" align="center" sortable>
                  <template #default="{ row }">
                    <el-tag effect="plain" size="small">#{{ row.staffId }}</el-tag>
                  </template>
                </el-table-column>

                <el-table-column prop="staffName" label="姓名" width="160" sortable>
                  <template #default="{ row }">
                    <div class="name-cell" @click="viewDetail(row)">
                      <div class="name-avatar">
                        {{ row.staffName?.charAt(0) || '?' }}
                      </div>
                      <div class="name-info">
                        <span class="name-text">{{ row.staffName }}</span>
                        <span class="status-dot active"></span>
                      </div>
                    </div>
                  </template>
                </el-table-column>

                <!-- ✅ 新增欄位：目前狀態 -->
                <el-table-column label="目前狀態" width="120" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getStaffWorkStatus(row.staffId).tagType" effect="light" round>
                      <i
                        :class="getStaffWorkStatus(row.staffId).icon"
                        style="margin-right: 6px"
                      ></i>
                      {{ getStaffWorkStatus(row.staffId).text }}
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column prop="staffCompany" label="所屬公司" min-width="160">
                  <template #default="{ row }">
                    <div v-if="row.staffCompany" class="company-cell">
                      <i class="fas fa-building company-icon"></i>
                      <span>{{ row.staffCompany }}</span>
                    </div>
                    <span v-else class="text-muted">未填寫</span>
                  </template>
                </el-table-column>

                <el-table-column prop="staffPhone" label="聯絡電話" width="150">
                  <template #default="{ row }">
                    <div v-if="row.staffPhone" class="phone-cell">
                      <i class="fas fa-phone phone-icon"></i>
                      <span>{{ row.staffPhone }}</span>
                    </div>
                    <span v-else class="text-muted">-</span>
                  </template>
                </el-table-column>

                <el-table-column prop="staffEmail" label="Email" min-width="200">
                  <template #default="{ row }">
                    <el-tooltip v-if="row.staffEmail" :content="row.staffEmail" placement="top">
                      <div class="email-cell">
                        <i class="fas fa-envelope email-icon"></i>
                        <span>{{ row.staffEmail }}</span>
                      </div>
                    </el-tooltip>
                    <span v-else class="text-muted">-</span>
                  </template>
                </el-table-column>

                <el-table-column
                  prop="staffNote"
                  label="備註"
                  min-width="120"
                  show-overflow-tooltip
                >
                  <template #default="{ row }">
                    <span v-if="row.staffNote" class="note-cell">{{ row.staffNote }}</span>
                    <span v-else class="text-muted">-</span>
                  </template>
                </el-table-column>

                <el-table-column
                  prop="createdAt"
                  label="建立日期"
                  width="120"
                  :formatter="formatDate"
                />

                <el-table-column label="操作" width="180" align="center">
                  <template #default="{ row }">
                    <div class="action-buttons">
                      <el-tooltip content="查看詳情" placement="top">
                        <el-button
                          type="info"
                          size="small"
                          circle
                          @click="viewDetail(row)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-eye"></i>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="編輯資料" placement="top">
                        <el-button
                          type="primary"
                          size="small"
                          circle
                          @click="router.push(`/admin/staff-form/${row.staffId}`)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-edit"></i>
                        </el-button>
                      </el-tooltip>
                      <el-tooltip content="停用人員" placement="top">
                        <el-button
                          type="danger"
                          size="small"
                          circle
                          @click="handleDelete(row)"
                          class="action-btn-item"
                        >
                          <i class="fas fa-user-slash"></i>
                        </el-button>
                      </el-tooltip>
                    </div>
                  </template>
                </el-table-column>

                <template #empty>
                  <el-empty
                    :description="
                      statusFilter === 'ALL' && !searchText
                        ? '目前沒有人員資料'
                        : '沒有符合條件的人員'
                    "
                  >
                    <template #image>
                      <div class="empty-icon">
                        <i class="fas fa-users-slash"></i>
                      </div>
                    </template>

                    <!-- ✅ 只有在「全部」且「完全沒資料」才顯示新增 -->
                    <el-button
                      v-if="statusFilter === 'ALL' && !searchText"
                      type="primary"
                      @click="handleAddNew"
                    >
                      <i class="fas fa-plus mr-1"></i> 新增第一位人員
                    </el-button>
                  </el-empty>
                </template>
              </el-table>

              <div class="pagination-wrapper" v-if="showPagination">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  :total="paginationTotal"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                />
              </div>
            </el-card>

            <div class="tips-bar mt-3">
              <el-alert type="info" :closable="false" show-icon>
                <template #title>
                  <span>💡 小提示：雙擊表格列可快速查看人員詳情；詳情四張卡可點進工單歷史</span>
                </template>
              </el-alert>
            </div>
          </div>
        </transition>
      </div>
    </section>

    <!-- ========== 轉移工單並刪除 Dialog ========== -->
    <el-dialog
      v-model="showTransferDialog"
      title="轉移工單並刪除人員"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="!transferLoading"
      @close="closeTransferDialog"
    >
      <div class="transfer-dialog-content">
        <el-alert type="warning" :closable="false" show-icon class="mb-4">
          <template #title>
            <strong>{{ transferForm.deleteStaffName }}</strong> 有未完成的工單
          </template>
          <template #default> 請選擇要將工單轉移給哪位人員後，才能刪除此人員。 </template>
        </el-alert>

        <el-form label-position="top">
          <el-form-item label="選擇接手人員" required>
            <el-select
              v-model="transferForm.targetStaffId"
              placeholder="請選擇接手人員"
              filterable
              style="width: 100%"
              :disabled="transferLoading"
            >
              <el-option
                v-for="staff in availableTargetStaff"
                :key="staff.staffId"
                :label="`${staff.staffName} (${staff.specialization || '未設定專長'})`"
                :value="staff.staffId"
              >
                <div class="transfer-option">
                  <span class="name">{{ staff.staffName }}</span>
                  <el-tag size="small" type="info">{{
                    staff.specialization || '未設定專長'
                  }}</el-tag>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>

        <div class="transfer-preview" v-if="transferForm.targetStaffId">
          <el-divider content-position="left">
            <el-icon><InfoFilled /></el-icon> 操作預覽
          </el-divider>
          <div class="preview-content">
            <p>
              <el-icon color="#E6A23C"><Right /></el-icon>
              <strong>{{ transferForm.deleteStaffName }}</strong> 的所有未完成工單 將轉移給
              <strong>{{
                availableTargetStaff.find((s) => s.staffId === transferForm.targetStaffId)
                  ?.staffName
              }}</strong>
            </p>
            <p>
              <el-icon color="#F56C6C"><Delete /></el-icon>
              然後刪除人員 <strong>{{ transferForm.deleteStaffName }}</strong>
            </p>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeTransferDialog" :disabled="transferLoading"> 取消 </el-button>
        <el-button
          type="danger"
          :loading="transferLoading"
          :disabled="!transferForm.targetStaffId"
          @click="handleTransferAndDelete"
        >
          <el-icon v-if="!transferLoading"><Check /></el-icon>
          確認轉移並刪除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.staff-list-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding-bottom: 40px;
}

.content-header {
  padding: 20px 1rem;
}

.page-title-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
}

.title-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  transition: all 0.4s ease;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.title-icon:hover {
  transform: scale(1.1) rotate(10deg);
}

.title-content {
  flex: 1;
  min-width: 200px;
}

.title-content h1 {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 700;
  color: #303133;
  background: linear-gradient(135deg, #303133 0%, #606266 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.title-content .subtitle {
  margin: 6px 0 0;
  font-size: 0.9rem;
  color: #909399;
}

.title-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.add-btn {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
}

/* 統計卡片 */
.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;
  cursor: pointer;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  z-index: 1;
}

.active-card .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
}

.filter-card .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
}

.stat-info {
  z-index: 1;
}

.stat-info h3 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: #303133;
}

.stat-info span {
  font-size: 0.85rem;
  color: #909399;
}

.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 80px;
  color: rgba(0, 0, 0, 0.03);
}

.pulse-animation {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.header-text {
  font-weight: 600;
  font-size: 1.1rem;
  color: #303133;
}

.search-input {
  width: 300px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px 0 0 10px;
}

.search-input :deep(.el-input-group__append) {
  border-radius: 0 10px 10px 0;
}

/* 表格樣式 */
.custom-table {
  --el-table-header-bg-color: #f8f9fa;
}

/* 避免 scrollbar 有時出現有時消失造成欄寬抖動 */
.custom-table :deep(.el-table__body-wrapper) {
  overflow-y: scroll;
}

/* 保險：讓 header/body 計算一致 */
.custom-table :deep(.el-table__header),
.custom-table :deep(.el-table__body),
.custom-table :deep(th),
.custom-table :deep(td) {
  box-sizing: border-box;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.name-cell:hover {
  background: #f0f9eb;
}

.name-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.name-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.name-text {
  font-weight: 500;
  color: #303133;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.active {
  background: #67c23a;
  box-shadow: 0 0 6px rgba(103, 194, 58, 0.6);
  animation: blink 2s infinite;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.company-cell,
.phone-cell,
.email-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.company-icon {
  color: #409eff;
}
.phone-icon {
  color: #67c23a;
}
.email-icon {
  color: #e6a23c;
}

.email-cell {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-cell {
  color: #606266;
  font-size: 13px;
}

/* 操作按鈕 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-btn-item {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-btn-item:hover {
  transform: scale(1.2);
}

/* 空狀態 */
.empty-icon {
  font-size: 64px;
  color: #dcdfe6;
  margin-bottom: 16px;
}

/* 分頁器 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}

/* 提示欄 */
.tips-bar :deep(.el-alert) {
  border-radius: 12px;
}

/* 過渡動畫 */
.slide-down-enter-active {
  transition: all 0.5s ease-out;
}
.slide-down-leave-active {
  transition: all 0.3s ease-in;
}
.slide-down-enter-from {
  transform: translateY(-30px);
  opacity: 0;
}
.slide-down-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

.zoom-fade-enter-active {
  transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.zoom-fade-leave-active {
  transition: all 0.3s ease-in;
}
.zoom-fade-enter-from {
  transform: scale(0.95);
  opacity: 0;
}
.zoom-fade-leave-to {
  transform: scale(0.98);
  opacity: 0;
}

/* 輔助類 */
.text-muted {
  color: #c0c4cc;
}
.mr-1 {
  margin-right: 4px;
}
.mr-2 {
  margin-right: 8px;
}
.ml-2 {
  margin-left: 8px;
}
.mb-4 {
  margin-bottom: 1.5rem;
}
.mt-3 {
  margin-top: 1rem;
}

/* ========== 轉移工單 Dialog 樣式 ========== */
.transfer-dialog-content {
  padding: 0 10px;
}

.transfer-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.transfer-option .name {
  font-weight: 500;
}

.transfer-preview {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef3 100%);
  border-radius: 8px;
  padding: 12px 16px;
  margin-top: 10px;
}

.transfer-preview .preview-content {
  font-size: 14px;
}

.transfer-preview .preview-content p {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 8px 0;
  color: #606266;
}

.transfer-preview .preview-content p strong {
  color: #303133;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}
</style>
