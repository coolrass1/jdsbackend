-- Update existing clients to have default values for conflict of interest fields
UPDATE clients 
SET has_conflict_of_interest = false 
WHERE has_conflict_of_interest IS NULL;

-- Verify the update
SELECT COUNT(*) as total_clients,
       SUM(CASE WHEN has_conflict_of_interest = true THEN 1 ELSE 0 END) as clients_with_conflict,
       SUM(CASE WHEN has_conflict_of_interest = false THEN 1 ELSE 0 END) as clients_without_conflict
FROM clients;
