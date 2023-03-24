## To do
Not sure if everything is contained

### Github
- Add Versionname to the release-branch
- Create release and there a tag on the release-branch wiht v1.7.4
- Publish release

### Google Console
- The release should appear in the internal test - if not, then something in the Github workflows failed 

## Fail in Deploy
This looked good:
```
 * Deploying app to github release...
Getting release on opengisch/QField
GitRelease(title="1.7.4 - Rockies") v1.7.4 https://uploads.github.com/repos/opengisch/QField/releases/32756953/assets{?name,label}
Uploading asset: /tmp/qfield-v1.7.4-armv7.apk
OK
```
And that failed:
```
 * Deploying app to github release...
Getting release on opengisch/QField
GitRelease(title="1.7.5 - Rockies") v1.7.5 https://uploads.github.com/repos/opengisch/QField/releases/33240495/assets{?name,label}
Uploading asset: /tmp/qfield-v1.7.5-armv7.apk
Traceback (most recent call last):
  File "/home/travis/virtualenv/python3.8.1/lib/python3.8/site-packages/urllib3/connectionpool.py", line 670, in urlopen
    httplib_response = self._make_request(
  File "/home/travis/virtualenv/python3.8.1/lib/python3.8/site-packages/urllib3/connectionpool.py", line 392, in _make_request
    conn.request(method, url, **httplib_request_kw)
  File "/opt/python/3.8.1/lib/python3.8/http/client.py", line 1230, in request
    self._send_request(method, url, body, headers, encode_chunked)
  File "/opt/python/3.8.1/lib/python3.8/http/client.py", line 1276, in _send_request
    self.endheaders(body, encode_chunked=encode_chunked)
  File "/opt/python/3.8.1/lib/python3.8/http/client.py", line 1225, in endheaders
    self._send_output(message_body, encode_chunked=encode_chunked)
  File "/opt/python/3.8.1/lib/python3.8/http/client.py", line 1043, in _send_output
    self.send(chunk)
  File "/opt/python/3.8.1/lib/python3.8/http/client.py", line 965, in send
    self.sock.sendall(data)
  File "/opt/python/3.8.1/lib/python3.8/ssl.py", line 1204, in sendall
    v = self.send(byte_view[count:])
  File "/opt/python/3.8.1/lib/python3.8/ssl.py", line 1173, in send
    return self._sslobj.write(data)
BrokenPipeError: [Errno 32] Broken pipe
```

So I did it manually.