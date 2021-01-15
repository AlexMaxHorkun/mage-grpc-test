<?php
function aggregate(array $results): array
{
    $result = ['totalMs' => 0, 'totalRequests' => 0, 'p90Ms' => 0, 'slowestMs' => 0, 'fastestMs' => 0];
    $count = count($results);
    usort($results, function (array $result1, array $result2) { return $result1['p90Ms'] <=> $result2['p90Ms']; });
    $p90th = $results[0]['p90Ms'];
    if ($count > 1) {
        $p90th = $results[round($count * 0.9) - 1]['p90Ms'];
    }
    $result['p90Ms'] = $p90th;
    foreach ($results as $data) {
        if ($result['totalMs'] < $data['totalMs']) $result['totalMs'] = $data['totalMs'];
        if ($result['totalRequests'] < $data['totalRequests']) $result['totalRequests'] = $data['totalRequests'];
        if ($result['slowestMs'] < $data['slowestMs']) $result['slowestMs'] = $data['slowestMs'];
        if ($result['fastestMs'] > $data['fastestMs'] || $result['fastestMs'] === 0) $result['fastestMs'] = $data['fastestMs'];
    }

    return $result;
}

$dirs = scandir(__DIR__.'/results');
foreach ($dirs as $dir) {
    if ($dir[0] === '.') continue;

    $files = scandir(__DIR__.'/results/' .$dir);
    $phps = [];
    $javas = [];
    $mages = [];
    foreach ($files as $file) {
        if ($file[0] === '.') continue;
        $data = json_decode(file_get_contents(__DIR__.'/results/' .$dir.'/'.$file), true);
        $phps[] = $data['phpReadTime'];
        $javas[] = $data['javaReadTime'];
        $mages[] = $data['mageReadTime'];
    }

    $result = [
        'phpReadTime' => aggregate($phps),
        'javaReadTime' => aggregate($javas),
        'mageReadTime' => aggregate($mages)
    ];

    file_put_contents(__DIR__.'/results/' .$dir.'/aggregated.json', json_encode($result));
}
