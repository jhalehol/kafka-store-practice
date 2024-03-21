
topics=("salesmen" "products" "sales")

function createTopics() {
  docker exec kafka1 kafka-topics --bootstrap-server localhost:29092 --create --partitions 3 --topic $1
}

for topic in ${topics[@]}; do
  createTopics $topic
done