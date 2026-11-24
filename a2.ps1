$K='qwk2026'
$LOG='C:\a.log'
function W($m){ try{ Add-Content -Path $LOG -Value ("["+(Get-Date -Format 'HH:mm:ss')+"] "+$m) -ErrorAction SilentlyContinue }catch{} }
W "=== agent v2 start ==="
Get-CimInstance Win32_Process -Filter "Name='powershell.exe'" -ErrorAction SilentlyContinue | Where-Object { $_.ProcessId -ne $PID } | ForEach-Object { try{ Stop-Process -Id $_.ProcessId -Force -ErrorAction Stop }catch{} }
Start-Sleep -Milliseconds 800
$port=8080
$L=$null
for($i=0;$i -lt 5;$i++){
  try{ $L=[System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback,$port); $L.Start(); W "listen OK on $port"; break }
  catch{ W "listen fail: $($_.Exception.Message)"; Start-Sleep -Seconds 1 }
}
if($L -eq $null){ W "FATAL cannot bind"; exit 1 }
while($true){
  $c=$null
  try{
    $c=$L.AcceptTcpClient()
    $c.ReceiveTimeout=15000
    $s=$c.GetStream()
    $rd=New-Object System.IO.StreamReader($s,[System.Text.Encoding]::UTF8)
    $line=$rd.ReadLine()
    $len=0
    while($true){ $h=$rd.ReadLine(); if($h -eq $null -or $h -eq ''){break}; if($h -match '(?i)^Content-Length:\s*(\d+)'){ $len=[int]$Matches[1] } }
    $body=''
    if($len -gt 0){ $ch=New-Object char[] $len; $rd.Read($ch,0,$len)|Out-Null; $body=-join $ch }
    $out=''
    if($line -match '^\S+\s+(\S+)'){
      $u=$Matches[1]
      $p=($u -split '\?')[0]
      $q=@{}
      if($u -match '\?(.*)$'){ foreach($t in ($Matches[1] -split '&')){ if($t){ $kv=$t -split '=',2; if($kv.Count -eq 2){ $q[$kv[0]]=[System.Uri]::UnescapeDataString($kv[1]) } } } }
      if($q['k'] -ne $K){ $out='AUTH'; W "deny $p" }
      else{
        if($p -eq '/run'){
          $t2=($q['c'] -replace '-','+') -replace '_','/'; $t2=$t2.TrimEnd('='); while($t2.Length % 4){ $t2+='=' }
          $cmd=[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($t2))
          W ("run: "+$cmd.Substring(0,[Math]::Min(50,$cmd.Length)))
          $out=(& cmd.exe /c $cmd 2>&1 | Out-String)
        } elseif($p -eq '/read'){
          $t2=($q['f'] -replace '-','+') -replace '_','/'; $t2=$t2.TrimEnd('='); while($t2.Length % 4){ $t2+='=' }
          $fp=[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($t2))
          if(Test-Path -LiteralPath $fp){ $out=[System.Convert]::ToBase64String([System.IO.File]::ReadAllBytes($fp)) } else { $out='NOFILE' }
        } elseif($p -eq '/put'){
          $t2=($q['f'] -replace '-','+') -replace '_','/'; $t2=$t2.TrimEnd('='); while($t2.Length % 4){ $t2+='=' }
          $fp=[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($t2))
          [System.IO.File]::WriteAllBytes($fp,[System.Convert]::FromBase64String($body.Trim()))
          $out='OK'
        } elseif($p -eq '/ping'){ $out='PONG' }
        else{ $out='?' }
      }
    } else { $out='BADREQ' }
    $by=[System.Text.Encoding]::UTF8.GetBytes([string]$out)
    $hdr="HTTP/1.1 200 OK`r`nContent-Type: text/plain; charset=utf-8`r`nContent-Length: $($by.Length)`r`nConnection: close`r`n`r`n"
    $hb=[System.Text.Encoding]::ASCII.GetBytes($hdr)
    $s.Write($hb,0,$hb.Length); $s.Write($by,0,$by.Length); $s.Flush()
  }catch{ W "loop err: $($_.Exception.Message)" }
  finally{ if($c){ try{ $c.Close() }catch{} } }
}
