<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class StatementUpload extends Model
{
    protected $fillable = [
        'uid', 'filename', 'path', 'storage_url', 'status',
        'rows_imported', 'error', 'default_category',
    ];
}
