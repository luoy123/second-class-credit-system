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
$summaryResp = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/credits/students/$studentId/summary"

Write-Host "Apply Result:"
$applyResp | ConvertTo-Json -Depth 8

Write-Host "`nSummary Result:"
$summaryResp | ConvertTo-Json -Depth 8
