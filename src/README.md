## Getting Started

In the project directory, follow these steps to integrate JSON data into the database:

1. Run Database Setup: `data_integration_db`
2. Run Data Integration (optional clean step): `data_integration [clean]` if necessary
3. Install Dependencies: `data_integration [install]`
4. Run the Application: `DataIntegrationApplication`

The application will automatically create all necessary database tables using Flyway migrations upon startup.

### Application Overview
This application downloads a file from the https://avaandmed.ariregister.rik.ee/et/avaandmete-allalaadimine site from the category "Ettevõtja rekvisiidid: Määrused (JSON; ...)" and integrates the JSON data into the database.
