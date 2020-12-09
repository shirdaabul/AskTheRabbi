package com.example.asktherabbi.message.Model;

public class User
    {
        private String id;
        private String name;
        private String imageUrl;
        private String status;
        private String search;

        public User(String id, String name, String imageUrl, String status, String search) {
            this.id = id;
            this.name = name;
            this.imageUrl = imageUrl;
            this.status = status;
            this.search = search;
        }

        public User() {}



        public String getName () {
            return name;
        }

        public void setName (String username){
            this.name = username;
        }

        public String getImageUrl () {
            return imageUrl;
        }

        public void setImageUrl (String imageURL){
            this.imageUrl = imageURL;
        }

        public String getStatus () {
            return status;
        }

        public void setStatus (String status){
            this.status = status;
        }

        public String getSearch () {
            return search;
        }

        public void setSearch (String search){
            this.search = search;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
