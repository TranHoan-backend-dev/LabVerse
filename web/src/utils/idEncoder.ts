/**
 * ID Encoder/Decoder utility
 * Matches backend implementation: Base64 URL-safe encoding without padding
 * 
 * Backend reference:
 * - GroupService: com.se1853_jv.util.IdEncoder
 * - ReadingService: com.se1853_jv.readingservice.util.IdEncoder (uses withoutPadding)
 * - PaperService: com.se1853_jv.service.EncoderService
 */

/**
 * Encode a string ID to Base64 URL-safe format (without padding)
 * @param id - The ID string to encode (e.g., UUID)
 * @returns Base64 URL-safe encoded string (without padding)
 */
export function encode(id: string): string {
    if (!id || id.length === 0) {
        return id;
    }
    
    try {
        // Convert string to UTF-8 bytes
        const utf8Bytes = new TextEncoder().encode(id);
        
        // Convert bytes to Base64 using btoa
        // First convert Uint8Array to string for btoa
        let binary = '';
        for (let i = 0; i < utf8Bytes.length; i++) {
            binary += String.fromCharCode(utf8Bytes[i]);
        }
        
        // Encode to Base64
        let base64 = btoa(binary);
        
        // Convert to URL-safe Base64 (replace + with -, / with _)
        base64 = base64.replace(/\+/g, '-').replace(/\//g, '_');
        
        // Remove padding (matches backend's withoutPadding())
        base64 = base64.replace(/=/g, '');
        
        return base64;
    } catch (error) {
        console.error('Error encoding ID:', error);
        return id;
    }
}

/**
 * Decode a Base64 URL-safe encoded string back to original ID
 * @param encoded - The Base64 URL-safe encoded string (without padding)
 * @returns Decoded original ID string, or null if decoding fails
 */
export function decode(encoded: string): string | null {
    if (!encoded || encoded.length === 0) {
        return encoded;
    }
    
    try {
        // Convert URL-safe Base64 back to standard Base64
        let base64 = encoded.replace(/-/g, '+').replace(/_/g, '/');
        
        // Add padding if needed (for proper Base64 decoding)
        // Base64 strings should be multiples of 4
        const paddingNeeded = (4 - (base64.length % 4)) % 4;
        base64 += '='.repeat(paddingNeeded);
        
        // Decode Base64 to binary string
        const binary = atob(base64);
        
        // Convert binary string to Uint8Array
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        
        // Convert UTF-8 bytes to string
        return new TextDecoder().decode(bytes);
    } catch (error) {
        console.error('Error decoding ID:', error);
        return null;
    }
}

/**
 * IdEncoder class (for compatibility with import style)
 * Matches the backend class structure
 */
export class IdEncoder {
    /**
     * Encode a string ID to Base64 URL-safe format (without padding)
     * @param id - The ID string to encode
     * @returns Base64 URL-safe encoded string
     */
    static encode(id: string): string {
        return encode(id);
    }
    
    /**
     * Decode a Base64 URL-safe encoded string back to original ID
     * @param encoded - The Base64 URL-safe encoded string
     * @returns Decoded original ID string, or null if decoding fails
     */
    static decode(encoded: string): string | null {
        return decode(encoded);
    }
}

