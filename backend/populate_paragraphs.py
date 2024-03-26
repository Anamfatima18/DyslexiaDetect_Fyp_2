# import database

# # Paragraphs for different age groups
# paragraphs = {
#     '4-6': [
#         "In the pond, there is a little fish. It swims fast. The fish has many colors. It likes to play in the water.",
#         "I have a yellow duck toy. It floats in the tub. When I bathe, the duck is with me. We have fun in the water.",
#         "There is a big tree in my yard. It has green leaves. Birds sit on the tree. I like to sit under the tree.",
#         "I have a friendly dog. His name is Max. Max likes to run. He wags his tail when happy.",
#         "At night, I see the moon. It is bright and round. The moon looks like a big circle. I watch it from my window.",
#         # Add more paragraphs as needed
#     ],
#     '7-8': [
#         "Once, I went to a forest. It was big and green. I saw tall trees and small bushes. Birds were singing. It was a fun adventure.",
#         "After rain, I saw a rainbow. It had many colors. Red, orange, yellow, green, blue, indigo, and violet. It was beautiful.",
#         "I have nice neighbors. They have two kids. We play together in the park. We ride bikes and play games.",
#         "My school went on a trip. We saw a museum. There were old things to see. I learned a lot.",
#         "In winter, it snows. The ground is white. I wear a coat and boots. I build a snowman with my friends.",
#         # Add more paragraphs as needed
#     ],
#     '9-12': [
#         "Near our town, there's a mysterious cave. Legends say it's filled with hidden treasures. It's surrounded by old, twisted trees and the sound of the wind echoes through its hollows.",
#         "Our family planned a journey to the mountains. As we ascended, the air grew crisp and the scenery was breathtaking. Pine trees covered the landscape, and distant peaks touched the clouds.",
#         "Our school held a science fair. Students created various projects: erupting volcanoes, solar system models, and experiments on water filtration. The fair buzzed with excitement and innovation.",
#         "One day, I found a lost puppy in the park. It was small, with brown fur and sad eyes. I decided to help it find its way home, discovering the importance of kindness and responsibility.",
#         "In the heart of the city, there’s an ancient library. Its walls are lined with old books, containing stories and knowledge of the past. It's a quiet, magical place, where every book holds a secret."
#         # Add more paragraphs as needed
#     ]
# }

# def populate_paragraphs():
#     for age_group, group_paragraphs in paragraphs.items():
#         for paragraph in group_paragraphs:
#             success = database.add_paragraph(age_group, paragraph)
#             if success:
#                 print(f"Successfully added paragraph for age group {age_group}")
#             else:
#                 print(f"Failed to add paragraph for age group {age_group}")

# if __name__ == '__main__':
#     populate_paragraphs()
