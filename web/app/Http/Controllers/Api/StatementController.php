<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Jobs\ProcessBankStatement;
use App\Models\StatementUpload;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

/**
 * @OA\Tag(name="Statements", description="Bank statement PDF upload and processing queue")
 */
class StatementController extends Controller
{
    /**
     * @OA\Get(
     *     path="/api/v1/statements",
     *     operationId="listStatements",
     *     summary="List statement upload jobs",
     *     tags={"Statements"},
     *     security={{"FirebaseToken":{}}},
     *     @OA\Response(response=200, description="List of upload jobs (max 20, newest first)"),
     *     @OA\Response(response=401, description="Unauthorized")
     * )
     */
    public function index(Request $request): JsonResponse
    {
        $uid  = $request->firebase_uid;
        $jobs = StatementUpload::where('uid', $uid)
            ->orderByDesc('created_at')
            ->take(20)
            ->get(['id', 'filename', 'status', 'rows_imported', 'error', 'created_at']);

        return response()->json(['data' => $jobs]);
    }

    /**
     * @OA\Post(
     *     path="/api/v1/statements",
     *     operationId="uploadStatement",
     *     summary="Upload a PDF bank statement",
     *     tags={"Statements"},
     *     security={{"FirebaseToken":{}}},
     *     @OA\RequestBody(required=true, @OA\MediaType(mediaType="multipart/form-data",
     *         @OA\Schema(required={"file"},
     *             @OA\Property(property="file", type="string", format="binary"),
     *             @OA\Property(property="default_category", type="integer")
     *         )
     *     )),
     *     @OA\Response(response=202, description="Queued for processing"),
     *     @OA\Response(response=401, description="Unauthorized"),
     *     @OA\Response(response=422, description="Validation error")
     * )
     */
    public function show(Request $request, int $id): JsonResponse
    {
        $uid    = $request->firebase_uid;
        $upload = StatementUpload::where('id', $id)->where('uid', $uid)->firstOrFail();

        return response()->json(['data' => [
            'id'           => $upload->id,
            'filename'     => $upload->filename,
            'status'       => $upload->status,
            'rows_imported'=> $upload->rows_imported,
            'storage_url'  => $upload->storage_url,
            'error'        => $upload->error,
            'created_at'   => $upload->created_at,
        ]]);
    }

    /**
     * @OA\Get(
     *     path="/api/v1/statements/{id}",
     *     operationId="getStatement",
     *     summary="Get a single statement job",
     *     tags={"Statements"},
     *     security={{"FirebaseToken":{}}},
     *     @OA\Parameter(name="id", in="path", required=true, @OA\Schema(type="integer")),
     *     @OA\Response(response=200, description="Job details"),
     *     @OA\Response(response=401, description="Unauthorized"),
     *     @OA\Response(response=404, description="Not found")
     * )
     */
    public function store(Request $request): JsonResponse
    {
        $request->validate([
            'file'             => 'required|file|mimes:pdf|max:10240',
            'default_category' => 'nullable|integer',
        ]);

        $uid  = $request->firebase_uid;
        $file = $request->file('file');

        $filename = $file->getClientOriginalName();
        $path     = $file->store("statements/{$uid}");

        $upload = StatementUpload::create([
            'uid'              => $uid,
            'filename'         => $filename,
            'path'             => $path,
            'status'           => 'pending',
            'default_category' => $request->input('default_category', 0),
        ]);

        ProcessBankStatement::dispatch($upload->id, $uid);

        return response()->json([
            'message' => 'Statement uploaded and queued for processing.',
            'data'    => [
                'id'       => $upload->id,
                'filename' => $upload->filename,
                'status'   => $upload->status,
            ],
        ], 202);
    }
}
