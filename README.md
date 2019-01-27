<p align="center">
    <img src="giffitea_logo.svg" alt="alt text" width="128">
</p>

Giffitea is an Android application that can identify your photographs and show you related GIFs.

**API Keys**

Giffitea uses the Cloud Vision and Giphy APIs to identify images and request GIFs. To get things up and running you'll need to add API keys for these services to the `gradle.properties` file within the project's root directory.

```gradle
giffitea_giphy_api_key="7dbdae26-5e86-4414-93c9-fc32a372dc43"
giffitea_cloud_vision_api_key="d716d5d2-0356-40a9-b1ef-b8dfd03e5154"
```