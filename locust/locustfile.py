from locust import HttpUser, task

class HelloWorldUser(HttpUser):
    @task
    def user(self):
        self.client.post("/endpoint", json=
        {
        "id": 132,
        "temperature": 500,
        "humidity":70,
        "gas":55,
        "userId":1
        })