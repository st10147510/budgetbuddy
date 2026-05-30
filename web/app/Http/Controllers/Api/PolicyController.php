<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\PolicyService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

/**
 * @OA\Tag(name="Policies", description="Versioned Terms & Conditions and Privacy Policy management")
 *
 * @OA\Info(
 *     title="BudgetBuddy API",
 *     version="1.0.0",
 *     description="REST API for the BudgetBuddy personal finance mobile app."
 * )
 * @OA\SecurityScheme(
 *     securityScheme="FirebaseToken",
 *     type="http",
 *     scheme="bearer",
 *     bearerFormat="Firebase ID Token"
 * )
 * @OA\Server(url="http://localhost:8000/api/v1", description="Local development")
 * @OA\Server(url="https://your-domain.com/api/v1", description="Production")
 */
class PolicyController extends Controller
{
    /**
     * @OA\Get(
     *     path="/api/v1/policies/current",
     *     operationId="getPolicyVersions",
     *     summary="Get current policy versions",
     *     tags={"Policies"},
     *     security={},
     *     @OA\Response(response=200, description="Current policy versions")
     * )
     */
    public function current(): JsonResponse
    {
        $service  = app(PolicyService::class);
        $versions = $service->getCurrentVersions();
        return response()->json(['data' => array_merge($versions, [
            'terms_content'   => $service->getContent('terms'),
            'privacy_content' => $service->getContent('privacy'),
        ])]);
    }

    /**
     * @OA\Post(
     *     path="/api/v1/policies/accept",
     *     operationId="acceptPolicy",
     *     summary="Record policy acceptance",
     *     tags={"Policies"},
     *     security={{"FirebaseToken":{}}},
     *     @OA\RequestBody(required=true, @OA\JsonContent(
     *         required={"type"},
     *         @OA\Property(property="type", type="string", enum={"terms","privacy","all"})
     *     )),
     *     @OA\Response(response=200, description="Acceptance recorded"),
     *     @OA\Response(response=401, description="Unauthorized"),
     *     @OA\Response(response=422, description="Validation error")
     * )
     */
    public function accept(Request $request): JsonResponse
    {
        $request->validate([
            'type' => 'required|in:terms,privacy,all',
        ]);

        $uid     = $request->firebase_uid;
        $service = app(PolicyService::class);

        if ($request->type === 'all') {
            $service->recordAllAcceptances($uid, 'android');
        } else {
            $service->recordAcceptance($uid, $request->type, 'android');
        }

        return response()->json(['message' => 'Acceptance recorded.']);
    }

    /**
     * @OA\Get(
     *     path="/api/v1/policies/status",
     *     operationId="getPolicyStatus",
     *     summary="Get per-user policy acceptance status",
     *     tags={"Policies"},
     *     security={{"FirebaseToken":{}}},
     *     @OA\Response(response=200, description="Acceptance status for the current user"),
     *     @OA\Response(response=401, description="Unauthorized")
     * )
     */
    public function status(Request $request): JsonResponse
    {
        $uid     = $request->firebase_uid;
        $service = app(PolicyService::class);
        $versions = $service->getCurrentVersions();

        return response()->json(['data' => [
            'terms_accepted'   => $service->hasUserAccepted($uid, 'terms'),
            'privacy_accepted' => $service->hasUserAccepted($uid, 'privacy'),
            'all_accepted'     => $service->hasUserAcceptedAll($uid),
            'current_versions' => $versions,
        ]]);
    }
}
