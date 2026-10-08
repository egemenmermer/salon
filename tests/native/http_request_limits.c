#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "http_request.h"
static void feed(http_request_t *r, const char *s) { http_request_add_data(r,s,(int)strlen(s)); }
int main(void) {
 http_request_t *r=http_request_init();
 feed(r,"OPTIONS * RTSP/1.0\r\nCSeq: 1\r\nContent-Length: 0\r\n\r\n");
 assert(http_request_is_complete(r));assert(!http_request_has_error(r));http_request_destroy(r);
 r=http_request_init();
 const char *fragmented="OPTIONS * RTSP/1.0\r\nCSeq: 1\r\nContent-Length: 0\r\n\r\n";
 for(size_t i=0;i<strlen(fragmented);i++) {
   char *byte=malloc(1); *byte=fragmented[i]; http_request_add_data(r,byte,1); free(byte);
 }
 assert(http_request_is_complete(r)); assert(!http_request_has_error(r));
 assert(!strcmp(http_request_get_protocol(r),"RTSP/1.0")); http_request_destroy(r);
 r=http_request_init();feed(r,"POST /pair-setup-pin RTSP/1.0\r\nCSeq: 1\r\nContent-Length: 16777217\r\n\r\n");
 char *block=malloc(1024*1024);memset(block,'x',1024*1024);
 for(int i=0;i<17;i++)http_request_add_data(r,block,1024*1024);
 assert(http_request_has_error(r));http_request_destroy(r);
 r=http_request_init();feed(r,"OPTIONS * RTSP/1.0\r\nX-Test: ");
 http_request_add_data(r,block,65537);assert(http_request_has_error(r));http_request_destroy(r);
 r=http_request_init();feed(r,"OPTIONS * RTSP/1.0\r\n");
 for(int i=0;i<140;i++)feed(r,"X-Test: x\r\n");
 assert(http_request_has_error(r));http_request_destroy(r);free(block);
 puts("PASS: valid and byte-fragmented RTSP, body limit, header limit, header count");return 0;
}
