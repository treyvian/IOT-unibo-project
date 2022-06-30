"""Generate the time series prediction required in the data analysis"""

import math
import sys, getopt
import argparse
import time
from datetime import datetime
import pandas as pd
import numpy as np
from fbprophet import Prophet

from utils import TO_PREDICT, BUCKET_NAME, INLFUX_URL, TOKEN, ORG

from influxdb_client import InfluxDBClient, Point, WriteOptions
from influxdb_client.client.write_api import SYNCHRONOUS

from utils import run_prophet, forecast_to_line

if __name__ == "__main__":
    
    parser = argparse.ArgumentParser(description='')
    parser.add_argument('--x', dest='x', type=int, default=0)
    args = parser.parse_args()

    period = args.x 


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

