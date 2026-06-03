<?php

namespace App\Services;

use Kreait\Firebase\Contract\Storage;

class FirebaseStorageService
{
    private Storage $storage;

    public function __construct()
    {
        $this->storage = app(Storage::class);
    }

    /**
     * Upload a local PDF to Firebase Storage.
     * Path: bank_statements/{uid}/{timestamp}_{filename}
     * Returns a long-lived download URL compatible with Firebase SDKs.
     */
    public function uploadStatement(string $uid, string $localPath, string $originalName): string
    {
        $timestamp = (int) (microtime(true) * 1000);
        $safeName  = preg_replace('/[^a-zA-Z0-9._-]/', '_', $originalName);
        $objectName = "bank_statements/{$uid}/{$timestamp}_{$safeName}";

        $bucket = $this->storage->getBucket();

        $object = $bucket->upload(
            fopen($localPath, 'rb'),
            [
                'name'          => $objectName,
                'contentType'   => 'application/pdf',
            ]
        );

        // Build a Firebase-compatible download URL using the storage token
        // (same format the Android Firebase SDK produces)
        $info  = $object->info();
        $token = $info['metadata']['firebaseStorageDownloadTokens']
              ?? $this->generateToken($object);

        $encodedName = rawurlencode($objectName);
        $bucketName  = $bucket->name();

        return "https://firebasestorage.googleapis.com/v0/b/{$bucketName}/o/{$encodedName}?alt=media&token={$token}";
    }

    private function generateToken(\Google\Cloud\Storage\StorageObject $object): string
    {
        // If no download token exists, update the object metadata to create one
        $token = bin2hex(random_bytes(16));
        $object->update([
            'metadata' => ['firebaseStorageDownloadTokens' => $token],
        ]);
        return $token;
    }
}
