import pandas as pd
from sklearn.metrics import mean_squared_error

from utils import TO_PREDICT, BUCKET_NAME, INLFUX_URL, TOKEN, ORG, get_correct_times

from influxdb_client import InfluxDBClient, Point, WriteOptions


if __name__ == "__main__":

    client = InfluxDBClient(url=INLFUX_URL, token=TOKEN, org=ORG)
    query_api = client.query_api()

    dfs_pred = []
    time_list = []
    for i in TO_PREDICT:
        df = pd.read_csv(f"predictions/{i}_.csv")
        time_list = df['ds'].tolist()
        dfs_pred.append(df)
    

    query_list = []
    for i in TO_PREDICT:
        query = 'from(bucket:"IOT_exam")' \
            ' |> range(start: 2022-06-26T00:00:00Z, stop: 2022-06-26T12:00:00Z)'\
            ' |> filter(fn: (r) => r._measurement == "point")' \
            f' |> filter(fn: (r) => r._field == "{i}")'
    
        query_list.append(query)

    # Query InfluxDB and return the results
    results = [client.query_api().query(org=ORG, query=query) for query in query_list]

    client.__del__()

    raws = []
    for result in results:
        raw = []
        for table in result:
            for record in table.records:
                raw.append((record.get_value(), record.get_time()))
        raws.append(raw)

    # Convert raw data to DataFrame

    dfs_true = []
    for raw, value in zip(raws, TO_PREDICT):
        df = pd.DataFrame(raw, columns=[value,'ds'], index=None)
        df['ds'] = df['ds'].values.astype('<M8[s]')
        
        df_true = get_correct_times(df, time_list)
        dfs_true.append(df_true)

    
    for true,pred,value in zip(dfs_true, dfs_pred, TO_PREDICT):
        print(value)
        mse = mean_squared_error(true[value], pred[value+'_'])
        print(mse)
    