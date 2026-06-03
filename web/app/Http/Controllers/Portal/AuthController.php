<?php

namespace App\Http\Controllers\Portal;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Kreait\Firebase\Contract\Auth;
use Kreait\Firebase\Exception\Auth\InvalidPassword;
use Kreait\Firebase\Exception\Auth\UserNotFound;
use Throwable;

class AuthController extends Controller
{
    public function showLogin()
    {
        if (session('portal_user')) {
            return redirect()->route('portal.dashboard');
        }
        return view('portal.login');
    }

    public function login(Request $request)
    {
        $request->validate([
            'email'    => 'required|email',
            'password' => 'required|string',
        ]);

        try {
            $auth = app(Auth::class);

            // Sign in via Firebase REST API through the SDK
            $signIn = $auth->signInWithEmailAndPassword(
                $request->email,
                $request->password,
            );

            $uid   = $signIn->firebaseUserId();
            $fbUser = $auth->getUser($uid);

            session([
                'portal_user' => [
                    'uid'         => $uid,
                    'email'       => $fbUser->email,
                    'displayName' => $fbUser->displayName,
                ],
            ]);

            return redirect()->route('portal.dashboard');

        } catch (InvalidPassword | UserNotFound) {
            return back()->with('error', 'Invalid email or password.')->withInput($request->only('email'));
        } catch (Throwable $e) {
            return back()->with('error', 'Sign-in failed: ' . $e->getMessage())->withInput($request->only('email'));
        }
    }

    public function logout(Request $request)
    {
        $request->session()->forget('portal_user');
        return redirect()->route('portal.login');
    }
}
