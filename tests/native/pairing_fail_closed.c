#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <openssl/rand.h>
#include "pairing.h"
static int fail_random;
int salon_test_RAND_bytes(unsigned char *p, int n) {
    return fail_random ? 0 : RAND_bytes(p, n);
}
static void begin(pairing_session_t *s, pairing_t *p) {
    // Upstream SRP rejects rare salts/verifiers with shorter BIGNUM encodings.
    // Retry boundedly so this negative-path test does not depend on that random encoding.
    for (int i = 0; i < 16; ++i) {
        const char *salt = NULL, *pk = NULL; int ns = 0, np = 0;
        int result = srp_new_user(s, p, "test", "6789", &salt, &ns, &pk, &np);
        free((void *)pk);
        if (result == 0) return;
    }
    assert(!"SRP fixture generation failed repeatedly");
}
int main(void) {
    fail_random = 1;
    assert(random_pin() == -1);
    fail_random = 0;
    int pin = random_pin(); assert(pin > 0 && pin < 10000);
    int result = 0;
    pairing_t *pairing = pairing_init_generate("salon-test", "", &result);
    assert(pairing);
    pairing_session_t *s = pairing_session_init(pairing); assert(s);
    unsigned char a[256] = {2}, proof[64] = {0}, epk[32] = {0}, tag[16] = {0};
    assert(srp_validate_proof(s, pairing, a, 256, proof, 20, 64) < 0);
    assert(srp_confirm_pair_setup(s, pairing, epk, tag) < 0);
    const char *salt = NULL, *pk = NULL; int salt_n = 0, pk_n = 0;
    fail_random = 1;
    assert(srp_new_user(s, pairing, "test", "6789", &salt, &salt_n, &pk, &pk_n) < 0);
    fail_random = 0;
    begin(s, pairing);
    // A valid GCM payload derived from an unverified zero session key must never authenticate.
    unsigned char zero_key[40] = {0}, hash[64], aes_key[16], aes_iv[16];
    const char *labels[] = {"Pair-Setup-AES-Key", "Pair-Setup-AES-IV"};
    for (int i = 0; i < 2; ++i) {
        sha_ctx_t *ctx = sha_init();
        sha_update(ctx, (const unsigned char *)labels[i], strlen(labels[i]));
        sha_update(ctx, zero_key, sizeof(zero_key)); sha_final(ctx, hash, NULL); sha_destroy(ctx);
        memcpy(i ? aes_iv : aes_key, hash, 16);
    }
    aes_iv[15]++;
    unsigned char fake_pk[32] = {1};
    assert(gcm_encrypt(fake_pk, 32, epk, aes_key, aes_iv, tag) == 32);
    assert(srp_confirm_pair_setup(s, pairing, epk, tag) < 0);
    assert(srp_validate_proof(s, pairing, a, 256, proof, 1, 64) < 0);
    assert(srp_validate_proof(s, pairing, a, 256, proof, 20, 64) < 0);
    assert(srp_confirm_pair_setup(s, pairing, epk, tag) < 0);
    pairing_session_destroy(s);
    s = pairing_session_init(pairing);
    begin(s, pairing);
    unsigned char zero_a[256] = {0};
    assert(srp_validate_proof(s, pairing, zero_a, 256, proof, 20, 64) < 0);
    pairing_session_destroy(s); // Cancelled before successful verification.
    pairing_destroy(pairing);
    puts("PASS: PIN RNG failure, out-of-order pairing, malformed/incorrect proof, zero public key and cancelled session");
}
