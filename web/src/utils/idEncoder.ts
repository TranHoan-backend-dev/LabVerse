/**
 * Utility for encoding and decoding IDs to match backend IdEncoder
 * Uses Base64 URL-safe encoding (without padding)
 */
export class IdEncoder {
    /**
     * Encode a string ID to Base64 URL-safe format
     * Matches backend IdEncoder.encode() which uses Base64.getUrlEncoder().encodeToString()
     * @param id String ID to encode
     * @returns Encoded string
     */
    static encode(id: string | null | undefined): string {
        if (!id) {
            return '';
        }
        try {
            // Convert string to UTF-8 bytes using encodeURIComponent
            // This handles all Unicode characters correctly
            const utf8Bytes = unescape(encodeURIComponent(id));
            // Convert to Base64
            const base64 = btoa(utf8Bytes);
            // Convert to URL-safe format: replace + with -, / with _, and remove padding =
            return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
        } catch (error) {
            console.error('Error encoding ID:', error);
            return id;
        }
    }

    /**
     * Decode a Base64 URL-safe encoded string back to original ID
     * @param encoded Encoded string
     * @returns Decoded string
     */
    static decode(encoded: string | null | undefined): string {
        if (!encoded) {
            return '';
        }
        try {
            // Add padding if needed
            let base64 = encoded.replace(/-/g, '+').replace(/_/g, '/');
            while (base64.length % 4) {
                base64 += '=';
            }
            // Decode from Base64
            const bytes = Uint8Array.from(atob(base64), c => c.charCodeAt(0));
            // Convert bytes to string (UTF-8)
            return new TextDecoder().decode(bytes);
        } catch (error) {
            console.error('Error decoding ID:', error);
            return encoded;
        }
    }
}

