"""Generate the time series prediction required in the data analysis"""

import pandas as pd
import numpy as np
import time
from datetime import datetime
from fbprophet import Prophet
import math
import sys, getopt

from utils import TO_PREDICT, BUCKET_NAME, INLFUX_URL, TOKEN, ORG

from influxdb_client import InfluxDBClient, Point, WriteOptions
from influxdb_client.client.write_api import SYNCHRONOUS

from utils import run_prophet, forecast_to_line

if __name__ == "__main__":
    argv = sys.argv[1:]
    period = 0

    try:
        opts, args = getopt.getopt(argv,"hx:")
    except getopt.GetoptError:
        print('01_fbprophet.py -x <num-to-predict>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('01_fbprophet.py -x <num-to-predict>')
            sys.exit()
        elif opt in ("-x"):
            period = int(arg)


    lines_list = []
    for v in TO_PREDICT:
        df = pd.read_csv(f"data/{v}.csv", index_col=0) 
        
        # Runs the prophet model and returns the predictions
        forecast = run_prophet(df, period)

        forecast = forecast.tail(period)
        forecast.reset_index(inplace=True, drop=True)

        # Add a measurement column to our DataFrame
        forecast[v] = v+"_pred"
        lines_list.append(forecast_to_line(forecast, v))
        
        forecast.rename(columns={"yhat":f'{v}_'}, inplace=True)
        forecast[['ds',f'{v}_']].to_csv(f"predictions/{v}_.csv")


    client = InfluxDBClient(url=INLFUX_URL, token=TOKEN, org=ORG)

    # Write the lines to your instance
    print("Wring prediction to bucket...")
    _write_client = client.write_api(write_options=WriteOptions     (batch_size=1000, 
        flush_interval=10_000,
        jitter_interval=2_000,
        retry_interval=5_000))
    for lines in lines_list:
        _write_client.write(BUCKET_NAME, ORG, lines)

    # Close the client
    _write_client.__del__()
    client.__del__()

    print("done")

