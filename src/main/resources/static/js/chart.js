 const ctx = document.getElementById('myChart').getContext('2d');
    const labels = [[${chartData.labels}]]; // Spring에서 넘겨주는 날짜 데이터
    const data = [[${chartData.data}]]; // Spring에서 넘겨주는 게시글 수 데이터

    const myChart = new Chart(ctx, {
        type: 'line', // 또는 'bar', 'pie' 등
        data: {
            labels:  ['data1' ,'data2']  //labels,
            datasets: [{
                label: '게시글 생성 수',
                data: data,
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });