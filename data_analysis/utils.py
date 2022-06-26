"""Utils file for the data analysis phase"""

import time
from fbprophet import Prophet
import pandas as pd
import datetime

# Define Auth parameters and connect to client
TOKEN = "vzHGyarqLYKp9O-sT2DeuE1kDOne-16Iox-Oy0GLiGsFua-5CqitjHoGvWoz3Lv39-2WrSzb_b2_c0_gK5IbQw=="
BUCKET_NAME = "IOT_exam"
ORG = "Unibo"

INLFUX_URL = "http://localhost:8086"

# Time series that needs to be predicted
TO_PREDICT = ["temperature", "humidity", "gas"]

def run_prophet(timeseries, period):
    """Fit and run the Prophet model
    Args:
        - timeseries: pandas dataframe containing the timeseries
        - period: number of values to predict
    return:
        The prediction made by the algorithm
    """

    """Short time series can definitely have sensitivity to the parameters, particularly the changepoint_prior_scale. This can significantly impact the prediction: A very small value will force a linear trend, while a large value will allow the trend to fluctuate quite a bit (and, if there is little data, possibly capture seasonal effects)"""
    model = Prophet(daily_seasonality=True, weekly_seasonality=False, 
    yearly_seasonality=False, changepoint_prior_scale=0.4, seasonality_prior_scale=20).fit(timeseries)

    forecast = model.make_future_dataframe(periods=period, freq="H")
    forecast = model.predict(forecast)

    return forecast


def forecast_to_line(forecast, measurement):
    """Convert the DataFrame to Line Protocol
    
    Args:
        -forecast: forecast made by prophet
        -measurement: string with the name of the measurement to forecast
    returns:
        - lines protocol type
    """

    cp = forecast[['ds','yhat','yhat_lower','yhat_upper',measurement]].copy()
    lines = [str(cp[measurement][d])
            + ",type=forecast" 
            + " " 
            + measurement + "_=" + str(cp["yhat"][d]) + ","
            + measurement + "_lower=" + str(cp["yhat_lower"][d]) + ","
            + measurement + "_upper=" + str(cp["yhat_upper"][d]) +
            " " + str(int(time.mktime(cp['ds'][d].timetuple()))) + "000000000" for d in range(len(cp))]
    return lines


def get_correct_times(df_true, time_list):
    
    df_first = df = pd.DataFrame()

    df_true['ds'] = pd.to_datetime(df_true['ds'])
    for i in time_list:
        stamp = pd.to_datetime(i) - datetime.timedelta(0,7)
        st = [stamp + datetime.timedelta(0,j) for j in range(15)]
        
        df = df_true[df_true['ds'].isin(st)]
        if len(df) > 0:
            df_first = pd.concat([df_first,df], axis=0)

    df_first.reset_index(inplace=True, drop=True) 

    return df_first        