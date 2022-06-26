import pandas as pd

from utils import TO_PREDICT, BUCKET_NAME, INLFUX_URL, TOKEN, ORG

from influxdb_client import InfluxDBClient, Point, WriteOptions

if __name__ == "__main__":

    client = InfluxDBClient(url=INLFUX_URL, token=TOKEN, org=ORG)
    query_api = client.query_api()

    # Create a Flux queries for each time series
    # The time stamp refers to the UTC time
    query_list = []
    for i in TO_PREDICT:
        query = 'from(bucket:"IOT_exam")' \
            ' |> range(start: 2022-06-22T20:00:00Z, stop: 2022-06-26T00:00:00Z)'\
            ' |> filter(fn: (r) => r._measurement == "point")' \
            f' |> filter(fn: (r) => r._field == "{i}")'
        
        query_list.append(query)


    # Query InfluxDB and return the results
    results = [client.query_api().query(org=ORG, query=query) for query in query_list]


    # Convert the results into a list
    raws = []
    for result in results:
        raw = []
        for table in result:
            for record in table.records:
                raw.append((record.get_value(), record.get_time()))
        raws.append(raw)

    # Convert raw data to DataFrame

    print("=== Writing data to csv ===")

    for raw, value in zip(raws, TO_PREDICT):
        df = pd.DataFrame(raw, columns=['y','ds'], index=None)
        df['ds'] = df['ds'].values.astype('<M8[s]')
        df.to_csv(f"data/{value}.csv")

    client.__del__()

