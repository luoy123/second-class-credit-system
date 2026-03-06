$baseUrl = "http://127.0.0.1:8080"

$studentBody = @{
    studentNo = "20260001"
    name = "测试学生"
    major = "软件工程"
    grade = "2026"
} | ConvertTo-Json

$studentResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/students" -ContentType "application/json" -Body $studentBody
$studentId = $studentResp.data.id

$activityBody = @{
    title = "校级志愿服务活动"
    category = "志愿服务"
    organizer = "校团委"
    activityDate = "2026-03-06"
    maxCredit = 1.5
} | ConvertTo-Json

$activityResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/activities" -ContentType "application/json" -Body $activityBody
$activityId = $activityResp.data.id

$applyBody = @{
    studentId = $studentId
    activityId = $activityId
    category = "志愿服务"
    remark = "联调脚本自动提交"
} | ConvertTo-Json

$applyResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/credits/apply" -ContentType "application/json" -Body $applyBody
$recordId = $applyResp.data.id
$pendingPageResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/students/$studentId/records/page?page=0&size=10&status=PENDING"
$pendingAdminPageResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/pending/page?page=0&size=10"

$approveBody = @{
    remark = "联调脚本自动审核通过"
} | ConvertTo-Json

$approveResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/credits/$recordId/approve" -ContentType "application/json" -Body $approveBody
$approvedPageResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/students/$studentId/records/page?page=0&size=10&status=APPROVED"
$summaryResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/students/$studentId/summary"
$categoryStatsResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/analytics/categories"
$majorStatsResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/analytics/majors"
$gradeStatsResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/analytics/grades"
$monthlyStatsResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/analytics/monthly?year=2026"
$rankingResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/analytics/ranking?topN=10"

$categoryCsvPath = "category_statistics.csv"
$rankingCsvPath = "student_ranking.csv"
Invoke-WebRequest -Method Get -Uri "$baseUrl/api/credits/analytics/export/categories" -OutFile $categoryCsvPath
Invoke-WebRequest -Method Get -Uri "$baseUrl/api/credits/analytics/export/ranking?topN=10" -OutFile $rankingCsvPath

Write-Host "Apply Result:"
$applyResp | ConvertTo-Json -Depth 8

Write-Host "`nApprove Result:"
$approveResp | ConvertTo-Json -Depth 8

Write-Host "`nPending Page Result:"
$pendingPageResp | ConvertTo-Json -Depth 8

Write-Host "`nPending Admin Page Result:"
$pendingAdminPageResp | ConvertTo-Json -Depth 8

Write-Host "`nApproved Page Result:"
$approvedPageResp | ConvertTo-Json -Depth 8

Write-Host "`nSummary Result:"
$summaryResp | ConvertTo-Json -Depth 8

Write-Host "`nCategory Stats Result:"
$categoryStatsResp | ConvertTo-Json -Depth 8

Write-Host "`nMajor Stats Result:"
$majorStatsResp | ConvertTo-Json -Depth 8

Write-Host "`nGrade Stats Result:"
$gradeStatsResp | ConvertTo-Json -Depth 8

Write-Host "`nMonthly Stats Result:"
$monthlyStatsResp | ConvertTo-Json -Depth 8

Write-Host "`nRanking Result:"
$rankingResp | ConvertTo-Json -Depth 8

Write-Host "`nCSV Export Files:"
Write-Host $categoryCsvPath
Write-Host $rankingCsvPath
